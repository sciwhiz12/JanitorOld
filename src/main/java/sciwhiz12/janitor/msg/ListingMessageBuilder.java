package sciwhiz12.janitor.msg;

import com.google.common.collect.ImmutableList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.msg.json.ListingMessage;
import sciwhiz12.janitor.msg.substitution.CustomSubstitutions;
import sciwhiz12.janitor.msg.substitution.IHasCustomSubstitutions;
import sciwhiz12.janitor.msg.substitution.SubstitutionMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ListingMessageBuilder<T> implements IHasCustomSubstitutions<ListingMessageBuilder<T>> {
    private final ListingMessage message;
    private final Map<String, Supplier<String>> customSubstitutions;
    private int amountPerPage = 10;
    private BiConsumer<T, CustomSubstitutions> entryApplier = (entry, sub) -> {};

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

    public ListingMessageBuilder<T> setEntryApplier(BiConsumer<T, CustomSubstitutions> entryApplier) {
        this.entryApplier = entryApplier;
        return this;
    }

    public ListingMessageBuilder<T> apply(Consumer<ListingMessageBuilder<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    public ListingMessageBuilder<T> with(final String argument, final Supplier<String> value) {
        this.customSubstitutions.put(argument, value);
        return this;
    }

    public void build(MessageChannel channel, TranslationMap translations, SubstitutionMap globalSubstitutions,
        Message triggerMessage, List<T> entries) {

        final CustomSubstitutions customSubs = globalSubstitutions.with(customSubstitutions);
        final ImmutableList<T> list = ImmutableList.copyOf(entries);
        final PagedMessage pagedMessage = new PagedMessage(message, list, amountPerPage);

        channel.sendMessage(pagedMessage.createMessage(translations, customSubs, entryApplier))
            .queue(listMsg -> translations.getBot().getReactionManager().newMessage(listMsg)
                .owner(triggerMessage.getAuthor().getIdLong())
                .removeEmotes(true)
                .add("\u2b05", (msg, event) -> { // PREVIOUS
                    if (pagedMessage.advancePage(PageDirection.PREVIOUS)) {
                        event.retrieveMessage()
                            .flatMap(eventMsg -> eventMsg.editMessage(
                                pagedMessage.createMessage(translations, customSubs, entryApplier))
                            )
                            .queue();
                    }
                })
                .add("\u274c", (msg, event) -> { // CLOSE
                    event.getChannel().deleteMessageById(event.getMessageIdLong())
                        .flatMap(v -> !triggerMessage.isFromGuild() ||
                                event.getGuild().getSelfMember()
                                    .hasPermission(triggerMessage.getTextChannel(),
                                        Permission.MESSAGE_MANAGE),
                            v -> triggerMessage.delete())
                        .queue();
                })
                .add("\u27a1", (msg, event) -> { // NEXT
                    if (pagedMessage.advancePage(PageDirection.NEXT)) {
                        event.retrieveMessage()
                            .flatMap(eventMsg -> eventMsg.editMessage(
                                pagedMessage.createMessage(translations, customSubs, entryApplier))
                            )
                            .queue();
                    }
                })
                .create()
            );
    }

    public void build(MessageChannel channel, JanitorBot bot, Message triggerMessage, List<T> entries) {
        build(channel, bot.getTranslations(), bot.getSubstitutions(), triggerMessage, entries);
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

        public MessageEmbed createMessage(TranslationMap translations, CustomSubstitutions substitutions,
            BiConsumer<T, CustomSubstitutions> applier) {
            if (currentPage != lastPage) {
                cachedMessage = message.create(
                    translations,
                    substitutions.with(new HashMap<>())
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
}
