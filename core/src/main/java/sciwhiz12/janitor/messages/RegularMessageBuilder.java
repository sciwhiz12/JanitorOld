package sciwhiz12.janitor.messages;

import com.google.common.primitives.Ints;
import joptsimple.internal.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import sciwhiz12.janitor.JanitorBotImpl;
import sciwhiz12.janitor.api.JanitorBot;
import sciwhiz12.janitor.api.messages.RegularMessage;
import sciwhiz12.janitor.api.messages.substitution.SubstitutionsMap;
import sciwhiz12.janitor.api.messages.substitution.Substitutor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegularMessageBuilder implements RegularMessage.Builder<RegularMessageBuilder> {
    private final RegularMessage message;
    private final Map<String, Supplier<String>> customSubstitutions;

    public RegularMessageBuilder(RegularMessage message, Map<String, Supplier<String>> customSubstitutions) {
        this.message = message;
        this.customSubstitutions = customSubstitutions;
    }

    public RegularMessageBuilder(RegularMessage message) {
        this(message, new HashMap<>());
    }

    public RegularMessageBuilder apply(Consumer<RegularMessageBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public RegularMessageBuilder with(final String argument, final Supplier<String> value) {
        customSubstitutions.put(argument, value);
        return this;
    }

    @Override
    public MessageEmbed build(SubstitutionsMap substitutions) {
        return create(message, substitutions.with(customSubstitutions)).build();
    }

    @Override
    public MessageEmbed build(JanitorBot bot) {
        return build(bot.getSubstitutions());
    }

    public MessageAction send(JanitorBotImpl bot, MessageChannel channel) {
        return channel.sendMessage(build(bot));
    }

    public static EmbedBuilder create(RegularMessage message, Substitutor subs) {
        final EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(subs.substitute(message.getTitle()), subs.substitute(message.getUrl()));
        builder.setColor(parseColor(subs.substitute(message.getColor())));
        builder.setAuthor(subs.substitute(message.getAuthorName()), subs.substitute(message.getAuthorUrl()), subs.substitute(
            message.getAuthorIconUrl()));
        builder.setDescription(subs.substitute(message.getDescription()));
        builder.setImage(subs.substitute(message.getImageUrl()));
        builder.setThumbnail(subs.substitute(message.getThumbnailUrl()));
        builder.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        builder.setFooter(subs.substitute(message.getFooterText()), subs.substitute(message.getFooterIconUrl()));
        for (MessageEmbed.Field field : message.getFields()) {
            builder.addField(subs.substitute(field.getName()), subs.substitute(field.getValue()), field.isInline());
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
