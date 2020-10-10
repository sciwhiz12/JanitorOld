package sciwhiz12.janitor.msg.json;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.OffsetDateTime;

public class ListingMessage {
    protected final String url;
    protected final String title;
    protected final String description;
    protected final OffsetDateTime timestamp;
    protected final int color;
    protected final MessageEmbed.Thumbnail thumbnail;
    protected final MessageEmbed.AuthorInfo author;
    protected final MessageEmbed.Footer footer;
    protected final MessageEmbed.ImageInfo image;
    protected final Multimap<FieldPlacement, MessageEmbed.Field> fields;

    @Deprecated
    public ListingMessage() {
        this(null, null, null, null, 0, null, null, null, null, null);
    }

    public ListingMessage(MessageEmbed embed) {
        this(embed.getUrl(),
            embed.getTitle(),
            embed.getDescription(),
            embed.getTimestamp(),
            embed.getColorRaw(),
            embed.getThumbnail(),
            embed.getAuthor(),
            embed.getFooter(),
            embed.getImage(),
            Multimaps.index(embed.getFields(), k -> FieldPlacement.BEFORE));
    }

    public ListingMessage(String url, String title, String description, OffsetDateTime timestamp, int color,
        MessageEmbed.Thumbnail thumbnail, MessageEmbed.AuthorInfo author, MessageEmbed.Footer footer,
        MessageEmbed.ImageInfo image, Multimap<FieldPlacement, MessageEmbed.Field> fields) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.color = color;
        this.thumbnail = thumbnail;
        this.author = author;
        this.footer = footer;
        this.image = image;
        this.fields = fields;
    }

    public enum ListingType {
        DESCRIPTION, FIELDS
    }

    public enum FieldPlacement {
        BEFORE, AFTER;
    }
}
