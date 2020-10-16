package sciwhiz12.janitor.msg.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class RegularMessageDeserializer extends StdDeserializer<RegularMessage> {
    public RegularMessageDeserializer() {
        super(RegularMessage.class);
    }

    @Override
    public RegularMessage deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {

        final JsonNode node = ctx.readTree(p);

        String title = null;
        String url = null;
        String description = node.path("description").asText(null);
        String color = node.path("color").asText(null);
        String authorName = null;
        String authorUrl = null;
        String authorIconUrl = null;
        String footerText = null;
        String footerIconUrl = null;
        String imageUrl = node.path("image").asText(null);
        String thumbnailUrl = node.path("thumbnail").asText(null);
        List<MessageEmbed.Field> fields = readFields(node);

        // Title
        if (node.path("title").isTextual()) {
            title = node.path("title").asText();
        } else if (node.path("title").path("text").isTextual()) {
            title = node.path("title").path("text").asText();
            url = node.path("title").path("url").asText(null);
        }

        // Author
        if (node.path("author").isTextual()) {
            authorName = node.path("author").asText();
        } else if (node.path("author").path("name").isTextual()) {
            authorName = node.path("author").path("name").asText();
            authorUrl = node.path("author").path("url").asText(null);
            authorIconUrl = node.path("author").path("icon_url").asText(null);
        }

        // Footer
        if (node.path("footer").isTextual()) {
            footerText = node.path("footer").asText();
        } else if (node.path("footer").path("text").isTextual()) {
            footerText = node.path("footer").path("text").asText();
            footerIconUrl = node.path("footer").path("icon_url").asText(null);
        }

        return new RegularMessage(title, url, description, color, authorName, authorUrl,
            authorIconUrl, footerText, footerIconUrl, imageUrl, thumbnailUrl, fields);
    }

    public static List<MessageEmbed.Field> readFields(JsonNode node) {
        if (node.path("fields").isArray()) {
            final ArrayList<MessageEmbed.Field> fields = new ArrayList<>();
            for (int i = 0; i < node.path("fields").size(); i++) {
                final MessageEmbed.Field field = readField(node.path("fields").path(i));
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
        if (fieldNode.path("name").isTextual() && fieldNode.path("value").isTextual()) {
            return new MessageEmbed.Field(
                fieldNode.path("name").asText(),
                fieldNode.path("value").asText(),
                fieldNode.path("inline").asBoolean(false)
            );
        }
        return null;
    }
}
