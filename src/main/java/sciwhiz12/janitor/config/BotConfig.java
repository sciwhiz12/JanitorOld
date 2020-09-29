package sciwhiz12.janitor.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static sciwhiz12.janitor.Logging.CONFIG;
import static sciwhiz12.janitor.Logging.JANITOR;

public class BotConfig {
    public static final Path DEFAULT_CONFIG_PATH = Path.of("config.toml");
    public static final String CLIENT_TOKEN = "discord.client_token";
    public static final String OWNER_ID = "discord.owner_id";
    public static final String TRANSLATION_FILE_PATH = "messages.translation_file";
    public static final String COMMAND_PREFIX = "commands.prefix";

    private final BotOptions options;
    private final Path configPath;
    private final CommentedFileConfig config;

    public BotConfig(BotOptions options) {
        this.options = options;
        this.configPath = options.getConfigPath().orElse(DEFAULT_CONFIG_PATH);
        this.config = CommentedFileConfig.builder(configPath, TomlFormat.instance())
            .defaultResource("default-config.toml")
            .preserveInsertionOrder()
            .build();
        try {
            JANITOR.info("Building config from {}", configPath);
            config.load();
            // TODO: config spec
            FileWatcher.defaultInstance().addWatch(configPath, this::onFileChange);
        }
        catch (IOException ex) {
            JANITOR.error("Error while building config from file {}", configPath, ex);
        }
    }

    public CommentedFileConfig getRawConfig() {
        return config;
    }

    @Nullable
    public Path getTranslationsFile() {
        return options.getTranslationsFile().
            or(() -> Optional.ofNullable(config.<String>get(TRANSLATION_FILE_PATH)).map(Path::of))
            .orElse(null);
    }

    public Optional<String> getToken() {
        return options.getToken().or(() -> config.getOptional(CLIENT_TOKEN));
    }

    public String getCommandPrefix() {
        return options.getCommandPrefix().orElseGet(() -> config.get(COMMAND_PREFIX));
    }

    public Optional<Long> getOwnerID() {
        return options.getOwnerID().or(() -> config.getOptional(OWNER_ID));
    }

    public void close() {
        config.close();
    }

    void onFileChange() {
        try {
            CONFIG.info("Reloading config due to file change {}", configPath);
            config.load();
        }
        catch (Exception ex) {
            CONFIG.error("Error while reloading config from {}", configPath, ex);
        }
    }
}
