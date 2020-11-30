package sciwhiz12.janitor.messages.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Joiner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import sciwhiz12.janitor.api.messages.ListingMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class ListingMessageDeserializer extends StdDeserializer<ListingMessage> {
    public static final Joiner NEWLINE = Joiner.on('\n');

    public ListingMessageDeserializer() {
        super(ListingMessage.class);
    }

    @Override
    public ListingMessage deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {

        final JsonNode root = ctx.readTree(p);

        String title = null;
        String url = null;
        String description = DeserializerUtil.readText(root.path("description"));
        String color = root.path("color").asText(null);
        String authorName = null;
        String authorUrl = null;
        String authorIconUrl = null;
        String footerText = null;
        String footerIconUrl = null;
        String imageUrl = root.path("image").asText(null);
        String thumbnailUrl = root.path("thumbnail").asText(null);
        String emptyText = root.path("empty").asText(null);
        List<MessageEmbed.Field> beforeFields = readFields(root.path("fields").path("before"));
        List<MessageEmbed.Field> afterFields = readFields(root.path("fields").path("after"));

        // Title
        if (root.path("title").isTextual()) {
            title = root.path("title").asText();
        } else if (root.path("title").path("text").isTextual()) {
            title = root.path("title").path("text").asText();
            url = root.path("title").path("url").asText(null);
        }

        // Author
        if (root.path("author").isTextual()) {
            authorName = root.path("author").asText();
        } else if (root.path("author").path("name").isTextual()) {
            authorName = root.path("author").path("name").asText();
            authorUrl = root.path("author").path("url").asText(null);
            authorIconUrl = root.path("author").path("icon_url").asText(null);
        }

        // Footer
        if (root.path("footer").isTextual()) {
            footerText = root.path("footer").asText();
        } else if (root.path("footer").path("text").isTextual()) {
            footerText = root.path("footer").path("text").asText();
            footerIconUrl = root.path("footer").path("icon_url").asText(null);
        }

        // ENTRY
        final ListingMessage.Entry entry = readEntry(root.path("entry"));

        return new ListingMessage(title, url, description, color, authorName, authorUrl, authorIconUrl, footerText,
            footerIconUrl, imageUrl, thumbnailUrl, emptyText, entry, beforeFields, afterFields);
    }

    public static ListingMessage.Entry readEntry(JsonNode root) {
        switch (root.path("type").asText()) {
            case "field": {
                return new ListingMessage.FieldEntry(
                    root.path("name").asText(EmbedBuilder.ZERO_WIDTH_SPACE),
                    root.path("value").asText(EmbedBuilder.ZERO_WIDTH_SPACE),
                    root.path("inline").asBoolean(false)
                );
            }
            default:
            case "description": {
                return new ListingMessage.DescriptionEntry(
                    root.path("joiner").asText(null),
                    root.path("text").asText());
            }
        }
    }

    public static List<MessageEmbed.Field> readFields(JsonNode node) {
        if (node.isArray()) {
            final ArrayList<MessageEmbed.Field> fields = new ArrayList<>();
            for (int i = 0; i < node.size(); i++) {
                final MessageEmbed.Field field = readField(node.path(i));
                if (field != null) {
                    fields.add(field);
                }
            }
            return fields;
        }
        return Collections.emptyList();
    }

    @Nullable
    public static MessageEmbed.Field readField(JsonNode fieldNode) {
        if (fieldNode.path("name").isTextual()) {
            return new MessageEmbed.Field(
                fieldNode.path("name").asText(),
                DeserializerUtil.readText(fieldNode.path("value")),
                fieldNode.path("inline").asBoolean(false)
            );
        }
        return null;
    }
}
