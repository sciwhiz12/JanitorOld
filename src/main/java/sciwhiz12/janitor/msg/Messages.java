package sciwhiz12.janitor.msg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.msg.json.ListingMessage;
import sciwhiz12.janitor.msg.json.RegularMessage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.io.Resources.getResource;
import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.MESSAGES;

public class Messages {
    public static final String JSON_FILE_SUFFIX = ".json";
    public static final String MESSAGES_FILENAME = "messages";
    public static final String DEFAULT_MESSAGES_FOLDER = "messages/";
    public static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    private final JanitorBot bot;
    private final Path messagesFolder;
    private final Map<String, RegularMessage> regularMessages = new HashMap<>();
    private final Map<String, ListingMessage> listingMessages = new HashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public Messages(JanitorBot bot, Path messagesFolder) {
        this.bot = bot;
        this.messagesFolder = messagesFolder;
        loadMessages();
    }

    public JanitorBot getBot() {
        return bot;
    }

    public void loadMessages() {
        boolean success = false;

        if (messagesFolder != null) {
            JANITOR.debug(MESSAGES, "Loading messages from folder {}", messagesFolder);
            success = loadMessages(
                path -> Files.newBufferedReader(messagesFolder.resolve(path + JSON_FILE_SUFFIX))
            );
        } else {
            JANITOR.info(MESSAGES, "No custom messages folder specified");
        }

        if (!success) {
            JANITOR.info(MESSAGES, "Loading default messages");
            //noinspection UnstableApiUsage
            loadMessages(
                file -> new InputStreamReader(getResource(DEFAULT_MESSAGES_FOLDER + file + JSON_FILE_SUFFIX).openStream())
            );
        }
    }

    boolean loadMessages(FileOpener files) {
        try (Reader keyReader = files.open(MESSAGES_FILENAME)) {
            List<String> keysList = jsonMapper.readValue(keyReader, LIST_TYPE);
            regularMessages.clear();
            for (String messageKey : keysList) {
                final String path = messageKey.replace("/", FileSystems.getDefault().getSeparator());
                try (Reader reader = files.open(path)) {
                    final JsonNode tree = jsonMapper.readTree(reader);
                    final String type = tree.path("type").asText("regular");
                    if ("regular".equals(type)) {
                        regularMessages.put(messageKey, jsonMapper.convertValue(tree, RegularMessage.class));
                    } else if ("listing".equals(type)) {
                        listingMessages.put(messageKey, jsonMapper.convertValue(tree, ListingMessage.class));
                    } else {
                        JANITOR.warn(MESSAGES, "Unknown message type {} for {}", tree.path("type").asText(), messageKey);
                    }
                } catch (Exception e) {
                    JANITOR.error(MESSAGES, "Error while loading message {}", path, e);
                }
            }
            JANITOR.info(MESSAGES, "Loaded {} messages", regularMessages.size());
            return true;
        } catch (Exception e) {
            JANITOR.error(MESSAGES, "Error while loading messages", e);
            return false;
        }
    }

    public Map<String, RegularMessage> getRegularMessages() {
        return Collections.unmodifiableMap(regularMessages);
    }

    public RegularMessageBuilder getRegularMessage(String messageKey) {
        final RegularMessage msg = regularMessages.get(messageKey);
        if (msg == null) {
            JANITOR.warn(MESSAGES, "Attempted to get unknown regular message with key {}", messageKey);
            return new RegularMessageBuilder(UNKNOWN_REGULAR_MESSAGE).with("key", () -> messageKey);
        }
        return new RegularMessageBuilder(msg);
    }

    public Map<String, ListingMessage> getListingMessages() {
        return listingMessages;
    }

    public <T> ListingMessageBuilder<T> getListingMessage(String messageKey) {
        final ListingMessage msg = listingMessages.get(messageKey);
        if (msg == null) {
            JANITOR.warn(MESSAGES, "Attempted to get unknown listing message with key {}", messageKey);
            return new ListingMessageBuilder<T>(UNKNOWN_LISTING_MESSAGE).with("key", () -> messageKey);
        }
        return new ListingMessageBuilder<>(msg);
    }

    interface FileOpener {
        Reader open(String filePath) throws IOException;
    }

    public static final RegularMessage UNKNOWN_REGULAR_MESSAGE = new RegularMessage(
        "UNKNOWN MESSAGE!",
        null,
        "A regular message was tried to be looked up, but was not found. Please report this to your bot " +
            "maintainer/administrator.",
        String.valueOf(0xFF0000),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Collections.singletonList(new MessageEmbed.Field("Message Key", "${key}", false))
    );

    public static final ListingMessage UNKNOWN_LISTING_MESSAGE = new ListingMessage(
        "UNKNOWN MESSAGE!",
        null,
        "A listing message was tried to be looked up, but was not found. " +
            "Please report this to your bot maintainer/administrator.",
        String.valueOf(0xFF0000),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        new ListingMessage.DescriptionEntry(null, ""),
        Collections.singletonList(new MessageEmbed.Field("Message Key", "${key}", false)),
        Collections.emptyList()
    );
}
