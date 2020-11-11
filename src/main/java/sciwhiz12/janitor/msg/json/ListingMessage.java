package sciwhiz12.janitor.msg.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.primitives.Ints;
import joptsimple.internal.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import sciwhiz12.janitor.msg.substitution.CustomSubstitutions;
import sciwhiz12.janitor.msg.substitution.ISubstitutor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;

@JsonDeserialize(using = ListingMessageDeserializer.class)
public class ListingMessage {
    @Nullable protected final String title;
    @Nullable protected final String url;
    @Nullable protected final String description;
    @Nullable protected final String color;
    @Nullable protected final String authorName;
    @Nullable protected final String authorUrl;
    @Nullable protected final String authorIconUrl;
    @Nullable protected final String footerText;
    @Nullable protected final String footerIconUrl;
    @Nullable protected final String imageUrl;
    @Nullable protected final String thumbnailUrl;
    @Nullable protected final String emptyText;
    protected final Entry entry;
    protected final List<MessageEmbed.Field> beforeFields;
    protected final List<MessageEmbed.Field> afterFields;

    public ListingMessage(
        @Nullable String title,
        @Nullable String url,
        @Nullable String description,
        @Nullable String color,
        @Nullable String authorName,
        @Nullable String authorUrl,
        @Nullable String authorIconUrl,
        @Nullable String footerText,
        @Nullable String footerIconUrl,
        @Nullable String imageUrl,
        @Nullable String thumbnailUrl,
        @Nullable String emptyText,
        Entry entry,
        List<MessageEmbed.Field> beforeFields,
        List<MessageEmbed.Field> afterFields
    ) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.color = color;
        this.authorName = authorName;
        this.authorUrl = authorUrl;
        this.authorIconUrl = authorIconUrl;
        this.footerText = footerText;
        this.footerIconUrl = footerIconUrl;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.emptyText = emptyText;
        this.entry = entry;
        this.beforeFields = beforeFields;
        this.afterFields = afterFields;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getColor() {
        return color;
    }

    @Nullable
    public String getAuthorName() {
        return authorName;
    }

    @Nullable
    public String getAuthorUrl() {
        return authorUrl;
    }

    @Nullable
    public String getAuthorIconUrl() {
        return authorIconUrl;
    }

    @Nullable
    public String getFooterText() {
        return footerText;
    }

    @Nullable
    public String getFooterIconUrl() {
        return footerIconUrl;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @Nullable
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Nullable
    public String getEmptyText() {
        return emptyText;
    }

    public Entry getEntry() {
        return entry;
    }

    public List<MessageEmbed.Field> getBeforeFields() {
        return beforeFields;
    }

    public List<MessageEmbed.Field> getAfterFields() {
        return afterFields;
    }

    public <T> EmbedBuilder create(
        ISubstitutor global,
        Iterable<T> iterable,
        BiConsumer<T, CustomSubstitutions> entryApplier
    ) {
        final EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(global.substitute(title), global.substitute(url));
        builder.setColor(parseColor(global.substitute(color)));
        builder.setAuthor(global.substitute(authorName), global.substitute(authorUrl), global.substitute(authorIconUrl));
        builder.setDescription(global.substitute(description));
        builder.setImage(global.substitute(imageUrl));
        builder.setThumbnail(global.substitute(thumbnailUrl));
        builder.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        builder.setFooter(global.substitute(footerText), global.substitute(footerIconUrl));
        for (MessageEmbed.Field field : beforeFields) {
            builder.addField(global.substitute(field.getName()), global.substitute(field.getValue()), field.isInline());
        }

        final CustomSubstitutions entrySubs = new CustomSubstitutions();
        final Function<String, String> entryFunc = str -> str != null ? entrySubs.substitute(global.substitute(str)) : null;
        int count = 0;
        for (T listEntry : iterable) {
            entryApplier.accept(listEntry, entrySubs);
            if (entry instanceof FieldEntry) {
                FieldEntry fieldEntry = (FieldEntry) entry;
                builder.addField(
                    entryFunc.apply(fieldEntry.getFieldName()),
                    entryFunc.apply(fieldEntry.getFieldValue()),
                    fieldEntry.isInline()
                );
            } else if (entry instanceof DescriptionEntry) {
                DescriptionEntry descEntry = (DescriptionEntry) entry;
                builder.getDescriptionBuilder().append(entryFunc.apply(descEntry.getDescription()));
                builder.getDescriptionBuilder().append(descEntry.getJoiner());
            }
            count++;
        }
        if (count < 1) {
            builder.getDescriptionBuilder().append(global.substitute(emptyText));
        }

        for (MessageEmbed.Field field : afterFields) {
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

    public interface Entry {}

    public static class DescriptionEntry implements Entry {
        public static final String DEFAULT_JOINER = "\n";
        private final String joiner;
        private final String descriptionEntry;

        public DescriptionEntry(@Nullable String joiner, String descriptionEntry) {
            this.joiner = joiner != null ? joiner : DEFAULT_JOINER;
            this.descriptionEntry = descriptionEntry;
        }

        public String getJoiner() {
            return joiner;
        }

        public String getDescription() {
            return descriptionEntry;
        }
    }

    public static class FieldEntry implements Entry {
        private final String fieldName;
        private final String fieldValue;
        private final boolean inline;

        public FieldEntry(String fieldName, String fieldValue, boolean inline) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.inline = inline;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldValue() {
            return fieldValue;
        }

        public boolean isInline() {
            return inline;
        }
    }
}
