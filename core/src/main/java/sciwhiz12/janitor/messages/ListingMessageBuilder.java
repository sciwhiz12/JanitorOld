package sciwhiz12.janitor.messages;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import joptsimple.internal.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import sciwhiz12.janitor.api.messages.ListingMessage;
import sciwhiz12.janitor.api.messages.emote.ReactionMessage;
import sciwhiz12.janitor.api.messages.substitution.ModifiableSubstitutor;
import sciwhiz12.janitor.api.messages.substitution.SubstitutionsMap;
import sciwhiz12.janitor.api.messages.substitution.Substitutor;
import sciwhiz12.janitor.messages.substitution.CustomSubstitutions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ListingMessageBuilder<T> implements ListingMessage.Builder<T> {
    private final ListingMessage message;
    private final Map<String, Supplier<String>> customSubstitutions;
    private int amountPerPage = 6;
    private boolean addDeletionReaction = true;
    private BiConsumer<T, ModifiableSubstitutor<?>> entryApplier = (entry, sub) -> {};

    public ListingMessageBuilder(ListingMessage message, Map<String, Supplier<String>> customSubstitutions) {
        this.message = message;
        this.customSubstitutions = customSubstitutions;
    }

    public ListingMessageBuilder(ListingMessage message) {
        this(message, new HashMap<>());
    }

    public ListingMessageBuilder<T> amountPerPage(int amountPerPage) {
        this.amountPerPage = amountPerPage;
        return this;
    }

    public ListingMessageBuilder<T> setEntryApplier(BiConsumer<T, ModifiableSubstitutor<?>> entryApplier) {
        this.entryApplier = entryApplier;
        return this;
    }

    public ListingMessageBuilder<T> apply(Consumer<ListingMessage.Builder<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    public ListingMessageBuilder<T> with(final String argument, final Supplier<String> value) {
        this.customSubstitutions.put(argument, value);
        return this;
    }

    public ListingMessageBuilder<T> deletionReaction(final boolean addDeletionReaction) {
        this.addDeletionReaction = addDeletionReaction;
        return this;
    }

    public RestAction<Message> build(MessageChannel channel,
        SubstitutionsMap globalSubstitutions,
        Message triggerMessage,
        boolean reply,
        List<T> entries) {

        final ModifiableSubstitutor<?> customSubs = globalSubstitutions.with(customSubstitutions);
        final ImmutableList<T> list = ImmutableList.copyOf(entries);
        final PagedMessage pagedMessage = new PagedMessage(message, list, amountPerPage);

        MessageAction action = channel.sendMessage(pagedMessage.createMessage(customSubs, entryApplier));
        if (reply) {
            action = action.reference(triggerMessage);
        }
        return action.flatMap(listMsg -> {
                ReactionMessage reactionMsg = globalSubstitutions.getBot().getReactions().newMessage(listMsg)
                    .owner(triggerMessage.getAuthor().getIdLong())
                    .removeEmotes(true)
                    .add("\u2b05", (msg, event) -> { // PREVIOUS
                        if (pagedMessage.advancePage(PageDirection.PREVIOUS)) {
                            event.retrieveMessage()
                                .flatMap(eventMsg -> eventMsg.editMessage(
                                    pagedMessage.createMessage(customSubs, entryApplier))
                                )
                                .queue();
                        }
                    });

                if (addDeletionReaction) {
                    reactionMsg.add("\u274c", (msg, event) -> { // CLOSE
                        event.getChannel().deleteMessageById(event.getMessageIdLong())
                            .flatMap(v -> !triggerMessage.isFromGuild() ||
                                    event.getGuild().getSelfMember()
                                        .hasPermission(triggerMessage.getTextChannel(),
                                            Permission.MESSAGE_MANAGE),
                                v -> triggerMessage.delete())
                            .queue();
                    });
                }

                reactionMsg.add("\u27a1", (msg, event) -> { // NEXT
                    if (pagedMessage.advancePage(PageDirection.NEXT)) {
                        event.retrieveMessage()
                            .flatMap(eventMsg -> eventMsg.editMessage(
                                pagedMessage.createMessage(customSubs, entryApplier))
                            )
                            .queue();
                    }
                });

                return reactionMsg.create(listMsg);
            }
        );
    }

    class PagedMessage {
        private final ListingMessage message;
        private final ImmutableList<T> list;
        private final int maxPages;
        private final int amountPerPage;
        private int currentPage = 0;
        private int lastPage = -1;
        private EmbedBuilder cachedMessage;

        PagedMessage(ListingMessage message, ImmutableList<T> list, int amountPerPage) {
            this.message = message;
            this.list = list;
            this.amountPerPage = amountPerPage;
            this.maxPages = Math.floorDiv(list.size(), ListingMessageBuilder.this.amountPerPage);
        }

        public int getMaxPages() {
            return maxPages;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public boolean advancePage(PageDirection direction) {
            if (direction == PageDirection.PREVIOUS && currentPage > 0) {
                currentPage -= 1;
                return true;
            } else if (direction == PageDirection.NEXT && currentPage < maxPages) {
                currentPage += 1;
                return true;
            }
            return false;
        }

        public MessageEmbed createMessage(ModifiableSubstitutor<?> substitutions,
            BiConsumer<T, ModifiableSubstitutor<?>> applier) {
            if (currentPage != lastPage) {
                cachedMessage = create(
                    message,
                    substitutions
                        .with("page.max", () -> String.valueOf(maxPages + 1))
                        .with("page.current", () -> String.valueOf(currentPage + 1)),
                    list.stream()
                        .skip(currentPage * amountPerPage)
                        .limit(amountPerPage)
                        .collect(Collectors.toList()),
                    applier);
                lastPage = currentPage;
            }
            return cachedMessage.build();
        }
    }

    enum PageDirection {
        PREVIOUS, NEXT
    }

    public static <T> EmbedBuilder create(
        ListingMessage message,
        Substitutor global,
        Iterable<T> iterable,
        BiConsumer<T, ModifiableSubstitutor<?>> entryApplier
    ) {
        final EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(global.substitute(message.getTitle()), global.substitute(message.getUrl()));
        builder.setColor(parseColor(global.substitute(message.getColor())));
        builder.setAuthor(global.substitute(message.getAuthorName()), global.substitute(message.getAuthorUrl()),
            global.substitute(message.getAuthorIconUrl()));
        builder.setDescription(global.substitute(message.getDescription()));
        builder.setImage(global.substitute(message.getImageUrl()));
        builder.setThumbnail(global.substitute(message.getThumbnailUrl()));
        builder.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        builder.setFooter(global.substitute(message.getFooterText()), global.substitute(message.getFooterIconUrl()));
        for (MessageEmbed.Field field : message.getBeforeFields()) {
            builder.addField(global.substitute(field.getName()), global.substitute(field.getValue()), field.isInline());
        }

        final ListingMessage.Entry entry = message.getEntry();

        final CustomSubstitutions entrySubs = new CustomSubstitutions();
        final Function<String, String> entryFunc = str -> str != null ? entrySubs.substitute(global.substitute(str)) : null;
        int count = 0;
        for (T listEntry : iterable) {
            entryApplier.accept(listEntry, entrySubs);
            if (entry instanceof ListingMessage.FieldEntry) {
                ListingMessage.FieldEntry fieldEntry = (ListingMessage.FieldEntry) entry;
                builder.addField(
                    entryFunc.apply(fieldEntry.getFieldName()),
                    entryFunc.apply(fieldEntry.getFieldValue()),
                    fieldEntry.isInline()
                );
            } else if (entry instanceof ListingMessage.DescriptionEntry) {
                ListingMessage.DescriptionEntry descEntry = (ListingMessage.DescriptionEntry) entry;
                builder.getDescriptionBuilder().append(entryFunc.apply(descEntry.getDescription()));
                builder.getDescriptionBuilder().append(descEntry.getJoiner());
            }
            count++;
        }
        if (count < 1) {
            builder.getDescriptionBuilder().append(global.substitute(message.getEmptyText()));
        }

        for (MessageEmbed.Field field : message.getAfterFields()) {
            builder.addField(global.substitute(field.getName()), global.substitute(field.getValue()), field.isInline());
        }
        return builder;
    }

    private static int parseColor(String str) {
        if (Strings.isNullOrEmpty(str)) return Role.DEFAULT_COLOR_RAW;
        if (str.startsWith("0x")) {
            // noinspection UnstableApiUsage
            final Integer res = Ints.tryParse(str.substring(2), 16);
            if (res != null) {
                return res;
            }
        }
        // noinspection UnstableApiUsage
        final Integer res = Ints.tryParse(str, 10);
        if (res != null) {
            return res;
        }
        return Role.DEFAULT_COLOR_RAW;
    }
}
