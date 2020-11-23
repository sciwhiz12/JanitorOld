package sciwhiz12.janitor.api.messages;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import sciwhiz12.janitor.api.JanitorBot;
import sciwhiz12.janitor.api.messages.substitution.ModifiableSubstitutions;
import sciwhiz12.janitor.api.messages.substitution.SubstitutionsMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;

public class RegularMessage {
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
    protected final List<MessageEmbed.Field> fields;

    public RegularMessage(
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
        List<MessageEmbed.Field> fields
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
        this.fields = new ArrayList<>(fields);
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

    public List<MessageEmbed.Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RegularMessage.class.getSimpleName() + "[", "]")
            .add("title='" + title + "'")
            .add("url='" + url + "'")
            .add("description='" + description + "'")
            .add("color='" + color + "'")
            .add("authorName='" + authorName + "'")
            .add("authorUrl='" + authorUrl + "'")
            .add("authorIconUrl='" + authorIconUrl + "'")
            .add("footerText='" + footerText + "'")
            .add("footerIconUrl='" + footerIconUrl + "'")
            .add("imageUrl='" + imageUrl + "'")
            .add("thumbnailUrl='" + thumbnailUrl + "'")
            .add("fields=" + fields)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegularMessage that = (RegularMessage) o;
        return Objects.equals(title, that.title) &&
            Objects.equals(url, that.url) &&
            Objects.equals(description, that.description) &&
            Objects.equals(color, that.color) &&
            Objects.equals(authorName, that.authorName) &&
            Objects.equals(authorUrl, that.authorUrl) &&
            Objects.equals(authorIconUrl, that.authorIconUrl) &&
            Objects.equals(footerText, that.footerText) &&
            Objects.equals(footerIconUrl, that.footerIconUrl) &&
            Objects.equals(imageUrl, that.imageUrl) &&
            Objects.equals(thumbnailUrl, that.thumbnailUrl) &&
            fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(title, url, description, color, authorName, authorUrl, authorIconUrl, footerText, footerIconUrl, imageUrl,
                thumbnailUrl, fields);
    }

    public interface Builder<T extends Builder<?>> extends ModifiableSubstitutions<T> {
        MessageEmbed build(SubstitutionsMap substitutions);

        MessageEmbed build(JanitorBot bot);

        default MessageAction send(JanitorBot bot, MessageChannel channel) {
            return channel.sendMessage(build(bot));
        }

    }
}
