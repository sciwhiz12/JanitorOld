package tk.sciwhiz12.janitor.core.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Preconditions;
import tk.sciwhiz12.janitor.api.core.config.BotConfig;
import tk.sciwhiz12.janitor.api.Logging;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;

public class BotConfigImpl implements BotConfig {
    public static final Path DEFAULT_CONFIG_PATH = Path.of("config.toml");

    private final CommentedConfigSpec.ConfigValue<String> CLIENT_TOKEN;
    private final CommentedConfigSpec.LongValue OWNER_ID;

    public final CommentedConfigSpec.ConfigValue<String> CONFIGS_PATH;

    public final CommentedConfigSpec.ConfigValue<String> STORAGE_PATH;
    public final CommentedConfigSpec.IntValue AUTOSAVE_INTERVAL;

    public final CommentedConfigSpec.ConfigValue<String> CUSTOM_MESSAGES_DIRECTORY;

    public final CommentedConfigSpec.ConfigValue<String> COMMAND_PREFIX;

    private final BotOptions options;
    private final Path configPath;
    private final CommentedConfigSpec spec;
    private final CommentedFileConfig config;

    public BotConfigImpl(BotOptions options) {
        this.options = options;

        final CommentedConfigSpec.Builder builder = new CommentedConfigSpec.Builder();

        builder.push("discord");
        CLIENT_TOKEN = builder
            .comment("The client secret/token for the bot user", "This must be set, or the application will not start up.")
            .define("client_token", "");
        OWNER_ID = builder
            .comment("The id of the bot owner; used for sending status messages and for bot administration commands.",
                "If 0, then the bot has no owner set.")
            .defineInRange("owner_id", 0L, Long.MIN_VALUE, Long.MAX_VALUE);
        builder.pop();

        CONFIGS_PATH = builder
            .comment("The folder where guild configs are kept.")
            .define("configs_path", "configs");

        builder.push("storage");
        STORAGE_PATH = builder
            .comment("The folder where per-guild storage is kept.")
            .define("main_path", "guild_storage");
        AUTOSAVE_INTERVAL = builder
            .comment("The interval between storage autosave checks, in seconds.")
            .defineInRange("autosave_internal", 20, 1, Integer.MAX_VALUE);
        builder.pop();

        CUSTOM_MESSAGES_DIRECTORY = builder
            .comment("A folder containing custom messages, with a 'messages.json' key file.",
                "If blank, no folder shall be loaded and defaults will be used.")
            .define("messages.custom_messages", "");

        COMMAND_PREFIX = builder
            .comment("The prefix for commands.")
            .define("commands.prefix", "!");

        spec = builder.build();

        this.configPath = options.getConfigPath().orElse(DEFAULT_CONFIG_PATH);
        this.config = CommentedFileConfig.builder(configPath, TomlFormat.instance())
            .onFileNotFound(FileNotFoundAction.CREATE_EMPTY)
            .preserveInsertionOrder()
            .build();
        try {
            Logging.JANITOR.info("Building config from {}", configPath);
            config.load();
            spec.setConfig(config);
            // TODO: config spec
            FileWatcher.defaultInstance().addWatch(configPath, this::onFileChange);
        } catch (IOException ex) {
            Logging.JANITOR.error("Error while building config from file {}", configPath, ex);
        }
        Preconditions.checkArgument(!getToken().isEmpty(), "Supply a client token through config or command line");
        Preconditions.checkArgument(options.getConfigsFolder().
            or(() -> Optional.ofNullable(Path.of(CONFIGS_PATH.get()))).isPresent(), "No guilds config folder defined");
    }

    public CommentedFileConfig getRawConfig() {
        return config;
    }

    @Override
    @Nullable
    public Path getMessagesFolder() {
        return options.getMessagesFolder().
            or(() -> CUSTOM_MESSAGES_DIRECTORY.get().isBlank() ?
                Optional.empty() :
                Optional.of(Path.of(CUSTOM_MESSAGES_DIRECTORY.get())))
            .orElse(null);
    }

    @Override
    public Path getConfigsFolder() {
        return options.getConfigsFolder().
            orElseGet(() -> Path.of(CONFIGS_PATH.get()));
    }

    @Override
    public String getToken() {
        return options.getToken().orElse(CLIENT_TOKEN.get());
    }

    @Override
    public String getCommandPrefix() {
        return options.getCommandPrefix().orElseGet(COMMAND_PREFIX::get);
    }

    @Override
    public Optional<Long> getOwnerID() {
        final long ret = options.getOwnerID().orElse(OWNER_ID.get());
        if (ret == 0) return Optional.empty();
        return Optional.of(ret);
    }

    public void close() {
        config.close();
    }

    void onFileChange() {
        try {
            Logging.CONFIG.info("Reloading config due to file change {}", configPath);
            config.load();
            spec.setConfig(config);
        } catch (Exception ex) {
            Logging.CONFIG.error("Error while reloading config from {}", configPath, ex);
        }
    }
}
