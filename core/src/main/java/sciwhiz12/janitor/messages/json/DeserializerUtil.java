package sciwhiz12.janitor.messages.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

import static java.util.stream.Collectors.joining;

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
}
