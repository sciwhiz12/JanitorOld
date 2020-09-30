package sciwhiz12.janitor.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
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

    private final CommentedConfigSpec.ConfigValue<String> CLIENT_TOKEN;
    private final CommentedConfigSpec.LongValue OWNER_ID;

    public final CommentedConfigSpec.ConfigValue<String> STORAGE_PATH;
    public final CommentedConfigSpec.IntValue AUTOSAVE_INTERVAL;

    public final CommentedConfigSpec.ConfigValue<String> CUSTOM_TRANSLATION_FILE;

    public final CommentedConfigSpec.ConfigValue<String> COMMAND_PREFIX;

    public final CommentedConfigSpec.BooleanValue WARNINGS_ENABLE;
    public final CommentedConfigSpec.BooleanValue WARNINGS_RESPECT_MOD_ROLES;
    public final CommentedConfigSpec.BooleanValue WARNINGS_PREVENT_WARNING_MODS;
    public final CommentedConfigSpec.BooleanValue WARNINGS_REMOVE_SELF_WARNINGS;

    private final BotOptions options;
    private final Path configPath;
    private final CommentedConfigSpec spec;
    private final CommentedFileConfig config;

    public BotConfig(BotOptions options) {
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

        builder.push("storage");
        STORAGE_PATH = builder
            .comment("The folder where per-guild storage is kept.")
            .define("main_path", "guild_storage");
        AUTOSAVE_INTERVAL = builder
            .comment("The interval between storage autosave checks, in seconds.")
            .defineInRange("autosave_internal", 20, 1, Integer.MAX_VALUE);
        builder.pop();

        CUSTOM_TRANSLATION_FILE = builder
            .comment("A file which contains custom translation keys to load for messages.",
                "If blank, no file shall be loaded.")
            .define("messages.custom_translations", "");

        COMMAND_PREFIX = builder
            .comment("The prefix for commands.")
            .define("commands.prefix", "!");

        builder.comment("Moderation settings").push("moderation");
        {
            builder.comment("Settings for the warnings system").push("warnings");
            WARNINGS_ENABLE = builder
                .comment("Whether to enable the warnings system. If disabled, the related commands are force-disabled.")
                .define("enable", true);
            WARNINGS_RESPECT_MOD_ROLES = builder
                .comment(
                    "Whether to prevent lower-ranked moderators (in the role hierarchy) from removing warnings issued by " +
                        "higher-ranked moderators.")
                .define("respect_mod_roles", false);
            WARNINGS_PREVENT_WARNING_MODS = builder
                .comment("Whether to prevent moderators from issuing warnings against other moderators.")
                .define("warn_other_moderators", false);
            WARNINGS_REMOVE_SELF_WARNINGS = builder
                .comment("Whether to allow moderators to remove warnings from themselves.")
                .define("remove_self_warnings", false);
            builder.pop();
        }
        builder.pop();

        spec = builder.build();

        this.configPath = options.getConfigPath().orElse(DEFAULT_CONFIG_PATH);
        this.config = CommentedFileConfig.builder(configPath, TomlFormat.instance())
            .onFileNotFound(FileNotFoundAction.CREATE_EMPTY)
            .preserveInsertionOrder()
            .build();
        try {
            JANITOR.info("Building config from {}", configPath);
            config.load();
            spec.setConfig(config);
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
            or(() -> CUSTOM_TRANSLATION_FILE.get().isBlank() ?
                Optional.empty() :
                Optional.of(Path.of(CUSTOM_TRANSLATION_FILE.get())))
            .orElse(null);
    }

    public String getToken() {
        return options.getToken().orElse(CLIENT_TOKEN.get());
    }

    public String getCommandPrefix() {
        return options.getCommandPrefix().orElseGet(COMMAND_PREFIX::get);
    }

    public Optional<Long> getOwnerID() {
        final Long ret = options.getOwnerID().orElse(OWNER_ID.get());
        if (ret == 0) return Optional.empty();
        return Optional.of(ret);
    }

    public void close() {
        config.close();
    }

    void onFileChange() {
        try {
            CONFIG.info("Reloading config due to file change {}", configPath);
            config.load();
            spec.setConfig(config);
        }
        catch (Exception ex) {
            CONFIG.error("Error while reloading config from {}", configPath, ex);
        }
    }
}
