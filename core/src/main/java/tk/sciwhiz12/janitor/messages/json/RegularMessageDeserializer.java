package tk.sciwhiz12.janitor.messages.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.dv8tion.jda.api.entities.MessageEmbed;
import tk.sciwhiz12.janitor.api.messages.RegularMessage;
import tk.sciwhiz12.janitor.utils.DeserializerUtil;

import java.io.IOException;
import java.util.List;

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
        String description = DeserializerUtil.readText(node.path("description"));
        String color = node.path("color").asText(null);
        String authorName = null;
        String authorUrl = null;
        String authorIconUrl = null;
        String footerText = null;
        String footerIconUrl = null;
        String imageUrl = node.path("image").asText(null);
        String thumbnailUrl = node.path("thumbnail").asText(null);
        List<MessageEmbed.Field> fields = DeserializerUtil.readFields(node.path("fields"));

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
}
