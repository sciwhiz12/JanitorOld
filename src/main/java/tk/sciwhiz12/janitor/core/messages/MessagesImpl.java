package tk.sciwhiz12.janitor.core.messages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import tk.sciwhiz12.janitor.core.JanitorBotImpl;
import tk.sciwhiz12.janitor.api.core.messages.ListingMessage;
import tk.sciwhiz12.janitor.api.core.messages.Messages;
import tk.sciwhiz12.janitor.api.core.messages.RegularMessage;
import tk.sciwhiz12.janitor.core.messages.json.ListingMessageDeserializer;
import tk.sciwhiz12.janitor.core.messages.json.RegularMessageDeserializer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tk.sciwhiz12.janitor.api.Logging.JANITOR;
import static tk.sciwhiz12.janitor.api.Logging.MESSAGES;

public class MessagesImpl implements Messages {
    public static final String JSON_FILE_SUFFIX = ".json";
    public static final String MESSAGE_LIST_FILENAME = "messages.json";
    public static final String MESSAGES_FOLDER = "messages/";
    public static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    private final JanitorBotImpl bot;

    private final Map<String, RegularMessage> defaultRegularMessages = new HashMap<>();
    private final Map<String, ListingMessage> defaultListingMessages = new HashMap<>();
    private final Map<String, RegularMessage> customRegularMessages = new HashMap<>();
    private final Map<String, ListingMessage> customListingMessages = new HashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public MessagesImpl(JanitorBotImpl bot) {
        this.bot = bot;
        SimpleModule messageModule = new SimpleModule();
        messageModule.addDeserializer(ListingMessage.class, new ListingMessageDeserializer());
        messageModule.addDeserializer(RegularMessage.class, new RegularMessageDeserializer());
        jsonMapper.registerModule(messageModule);
        loadMessages();
    }

    public JanitorBotImpl getBot() {
        return bot;
    }

    public void loadMessages() {
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();

        JANITOR.info(MESSAGES, "Loading default messages");
        defaultListingMessages.clear();
        ctxLoader.resources(MESSAGES_FOLDER + MESSAGE_LIST_FILENAME)
            .forEach(url -> {
                JANITOR.info("Loading messages from {}", url.getPath());
                try (Reader keyReader = new InputStreamReader(url.openStream())) {
                    int loadedCount = 0;
                    for (String messageKey : jsonMapper.readValue(keyReader, LIST_TYPE)) {
                        InputStream resourceStream = ctxLoader
                            .getResourceAsStream(MESSAGES_FOLDER + messageKey + JSON_FILE_SUFFIX);
                        if (resourceStream == null) {
                            JANITOR.warn("Defined message {} cannot be found", messageKey);
                            continue;
                        }
                        loadedCount += readMessage(messageKey,
                            () -> new InputStreamReader(resourceStream),
                            defaultRegularMessages,
                            defaultListingMessages);
                    }
                    JANITOR.debug(MESSAGES, "Loaded {} messages", loadedCount);
                } catch (Exception e) {
                    JANITOR.error(MESSAGES, "Error while loading default messages from {}", url.getPath(), e);
                }
            });

        Path messagesFolder = bot.getBotConfig().getMessagesFolder();
        if (messagesFolder != null) {
            JANITOR.info(MESSAGES, "Loading custom messages from folder {}", messagesFolder);
            try (Reader keyReader = Files.newBufferedReader(messagesFolder.resolve(MESSAGE_LIST_FILENAME))) {
                int loadedCount = 0;
                for (String messageKey : jsonMapper.readValue(keyReader, LIST_TYPE)) {
                    final Path messagePath = messagesFolder.resolve(messageKey + JSON_FILE_SUFFIX);
                    if (Files.notExists(messagePath)) {
                        JANITOR.warn("Defined message {} cannot be found", messageKey);
                        continue;
                    }
                    readMessage(messageKey, () -> Files.newBufferedReader(messagePath), customRegularMessages,
                        customListingMessages);
                    loadedCount++;
                }
                JANITOR.debug(MESSAGES, "Loaded {} messages", loadedCount);
            } catch (Exception e) {
                JANITOR.error(MESSAGES, "Error while loading custom messages", e);
            }
        } else {
            JANITOR.info(MESSAGES, "No custom messages folder specified");
        }
    }

    @FunctionalInterface
    interface ThrowableSupplier<T, E extends Throwable> {
        T get() throws E;
    }

    private int readMessage(String messageKey, ThrowableSupplier<Reader, Exception> input,
        Map<String, RegularMessage> regularMessages, Map<String, ListingMessage> listingMessages) {
        try {
            final JsonNode tree = jsonMapper.readTree(input.get());
            final String type = tree.path("type").asText("regular");
            switch (type) {
                case "regular": {
                    regularMessages.put(messageKey, jsonMapper.convertValue(tree, RegularMessage.class));
                    break;
                }
                case "listing": {
                    listingMessages.put(messageKey, jsonMapper.convertValue(tree, ListingMessage.class));
                    break;
                }
                default: {
                    JANITOR.warn(MESSAGES, "Unknown message type {} for {}", tree.path("type").asText(), messageKey);
                    return 0;
                }
            }
            return 1;
        } catch (Exception e) {
            JANITOR.error(MESSAGES, "Error while loading message {}", messageKey, e);
            return 0;
        }
    }

    public RegularMessageBuilder getRegularMessage(String messageKey) {
        RegularMessage msg = customRegularMessages.get(messageKey);
        if (msg == null) {
            msg = defaultRegularMessages.get(messageKey);
        }
        if (msg == null) {
            JANITOR.warn(MESSAGES, "Attempted to get unknown regular message with key {}", messageKey);
            return new RegularMessageBuilder(UNKNOWN_REGULAR_MESSAGE).with("key", () -> messageKey);
        }
        return new RegularMessageBuilder(msg);
    }

    public <T> ListingMessageBuilder<T> getListingMessage(String messageKey) {
        ListingMessage msg = customListingMessages.get(messageKey);
        if (msg == null) {
            msg = defaultListingMessages.get(messageKey);
        }
        if (msg == null) {
            JANITOR.warn(MESSAGES, "Attempted to get unknown listing message with key {}", messageKey);
            return new ListingMessageBuilder<T>(UNKNOWN_LISTING_MESSAGE).with("key", () -> messageKey);
        }
        return new ListingMessageBuilder<>(msg);
    }
}
