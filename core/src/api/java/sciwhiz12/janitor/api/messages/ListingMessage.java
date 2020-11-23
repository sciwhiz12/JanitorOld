package sciwhiz12.janitor.api.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import sciwhiz12.janitor.api.JanitorBot;
import sciwhiz12.janitor.api.messages.substitution.ModifiableSubstitutions;
import sciwhiz12.janitor.api.messages.substitution.ModifiableSubstitutor;
import sciwhiz12.janitor.api.messages.substitution.SubstitutionsMap;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

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

    public interface Builder<T> extends ModifiableSubstitutions<Builder<T>> {
        Builder<T> amountPerPage(int amountPerPage);

        Builder<T> setEntryApplier(BiConsumer<T, ModifiableSubstitutor<?>> entryApplier);

        Builder<T> apply(Consumer<Builder<T>> consumer);

        Builder<T> with(final String argument, final Supplier<String> value);

        void build(MessageChannel channel,
            SubstitutionsMap globalSubstitutions,
            Message triggerMessage,
            List<T> entries);

        default void build(MessageChannel channel, JanitorBot bot, Message triggerMessage, List<T> entries) {
            build(channel, bot.getSubstitutions(), triggerMessage, entries);
        }
    }
}
