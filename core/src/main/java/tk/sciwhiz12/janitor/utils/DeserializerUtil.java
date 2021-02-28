package tk.sciwhiz12.janitor.utils;

import com.fasterxml.jackson.databind.JsonNode;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

import static java.util.stream.Collectors.joining;
import static net.dv8tion.jda.api.EmbedBuilder.ZERO_WIDTH_SPACE;

public final class DeserializerUtil {
    public static final String NEWLINE = "\n";

    private DeserializerUtil() {}

    @Nullable
    public static String readText(JsonNode node) {
        if (node.isTextual() || node.isValueNode()) {
            return node.asText();
        } else if (node.isArray()) {
            return StreamSupport.stream(node.spliterator(), false)
                .map(JsonNode::asText)
                .collect(joining(NEWLINE));
        } else if (node.isObject()) {
            final String joiner = node.path("joiner").asText(NEWLINE);
            return StreamSupport.stream(node.path("text").spliterator(), false)
                .map(JsonNode::asText)
                .collect(joining(NEWLINE));
        }
        return null;
    }

    public static List<MessageEmbed.Field> readFields(JsonNode root) {
        List<MessageEmbed.Field> fields = new ArrayList<>();

        if (root.isArray()) {
            for (JsonNode node : root) {
                if (node.isNull()) {
                    fields.add(new MessageEmbed.Field(ZERO_WIDTH_SPACE, ZERO_WIDTH_SPACE, false));
                } else if (node.isObject()) {
                    fields.add(new MessageEmbed.Field(
                        node.path("name").asText(ZERO_WIDTH_SPACE),
                        node.path("value").asText(ZERO_WIDTH_SPACE),
                        node.path("inline").asBoolean(true)
                    ));
                }
            }
        } else if (root.isObject()) {
            for (Iterator<String> names = root.fieldNames(); names.hasNext(); ) {
                final String name = names.next();
                final JsonNode node = root.get(name);
                final String fieldName = node.path("name").asText(name);
                String value = ZERO_WIDTH_SPACE;
                if (node.isValueNode()) {
                    value = node.asText();
                }
                final String fieldValue = node.path("value").asText(value);
                final boolean inline = node.path("inline").asBoolean(true);
                fields.add(new MessageEmbed.Field(fieldName, fieldValue, inline));
            }
        }

        return fields;
    }
}
