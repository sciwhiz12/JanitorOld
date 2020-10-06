package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import sciwhiz12.janitor.JanitorBot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static sciwhiz12.janitor.msg.Substitutions.substitute;

public class MessageBuilder {
    private final EmbedBuilder embedBuilder;
    private final Map<String, Supplier<String>> substitutions;

    public MessageBuilder(EmbedBuilder embedBuilder, Map<String, Supplier<String>> substitutions) {
        this.embedBuilder = embedBuilder;
        this.substitutions = substitutions;
    }

    public MessageBuilder() {
        this(new EmbedBuilder(), new HashMap<>());
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public MessageBuilder(MessageBuilder copy) {
        this(new EmbedBuilder(copy.embedBuilder), new HashMap<>(copy.substitutions));
    }

    public EmbedBuilder embed() {
        return embedBuilder;
    }

    public MessageBuilder embed(Consumer<EmbedBuilder> operator) {
        operator.accept(embed());
        return this;
    }

    public MessageBuilder apply(Consumer<MessageBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public MessageBuilder with(final String argument, final Supplier<String> value) {
        substitutions.put(argument, value);
        return this;
    }

    public MessageBuilder field(final String head, final boolean inline) {
        embedBuilder.addField(head + ".name", head + ".value", inline);
        return this;
    }

    public MessageBuilder blankField(final boolean inline) {
        embedBuilder.addBlankField(inline);
        return this;
    }

    public MessageEmbed build(Translations translations, Substitutions substitutions) {
        EmbedBuilder realEmbed = new EmbedBuilder();
        MessageEmbed tempEmbed = embed().build();
        final Map<String, Supplier<String>> replaceMap = substitutions.createDefaultedMap(this.substitutions);
        final UnaryOperator<String> replacer = str -> substitute(translations.translate(str), replaceMap);

        realEmbed.setColor(tempEmbed.getColorRaw());
        realEmbed.setTimestamp(tempEmbed.getTimestamp());
        if (tempEmbed.getTitle() != null)
            realEmbed.setTitle(replacer.apply(tempEmbed.getTitle()), tempEmbed.getUrl());
        if (tempEmbed.getThumbnail() != null)
            realEmbed.setThumbnail(tempEmbed.getThumbnail().getUrl());
        if (tempEmbed.getAuthor() != null)
            realEmbed.setAuthor(
                replacer.apply(tempEmbed.getAuthor().getName()),
                tempEmbed.getAuthor().getUrl(),
                tempEmbed.getAuthor().getIconUrl()
            );
        if (tempEmbed.getFooter() != null)
            realEmbed.setFooter(
                replacer.apply(tempEmbed.getFooter().getText()),
                tempEmbed.getFooter().getIconUrl()
            );
        if (tempEmbed.getImage() != null)
            realEmbed.setImage(tempEmbed.getImage().getUrl());
        if (tempEmbed.getDescription() != null)
            realEmbed.setDescription(replacer.apply(tempEmbed.getDescription()));

        for (MessageEmbed.Field field : tempEmbed.getFields())
            realEmbed.addField(
                replacer.apply(field.getName()),
                replacer.apply(field.getValue()),
                field.isInline()
            );

        return realEmbed.build();
    }

    public MessageEmbed build(JanitorBot bot) {
        return build(bot.getTranslations(), bot.getSubstitutions());
    }
}
