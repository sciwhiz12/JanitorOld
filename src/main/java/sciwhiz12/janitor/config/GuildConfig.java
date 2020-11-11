package sciwhiz12.janitor.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Joiner;
import net.dv8tion.jda.api.entities.GuildChannel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static sciwhiz12.janitor.Logging.CONFIG;
import static sciwhiz12.janitor.Logging.JANITOR;

public class GuildConfig {
    private static final Joiner NEWLINE = Joiner.on("\n");

    private final long guild;
    private final CommentedFileConfig config;
    private boolean closed = false;

    public static final ConfigNode<Boolean> ENABLE = new ConfigNode<>(
        "enable", () -> true,
        "Whether the bot is enabled for this guild.",
        "Can be used to temporarily disable the bot in emergency situations.");

    public static final ConfigNode<String> COMMAND_PREFIX = new ConfigNode<>(
        "commands.prefix", () -> "!",
        "The prefix for all commands.");

    public static final ConfigNode<Boolean> ENABLE_WARNS = new ConfigNode<>(
        "moderation.warns.enable", () -> true,
        "Whether to enable the warnings system. If disabled, the related commands are force-disabled.");

    public static final ConfigNode<Boolean> WARNS_RESPECT_MOD_ROLES = new ConfigNode<>(
        "moderation.warns.respect_mod_roles", () -> false,
        "Whether to prevent lower-ranked moderators (in the role hierarchy) from removing warnings " +
            "issued by higher-ranked moderators.");
    public static final ConfigNode<Boolean> ALLOW_WARN_OTHER_MODERATORS = new ConfigNode<>(
        "moderation.warns.warn_other_moderators", () -> true,
        "Whether to allow moderators to issue warnings against other moderators.");
    public static final ConfigNode<Boolean> ALLOW_REMOVE_SELF_WARNINGS = new ConfigNode<>(
        "moderation.warns.remove_self_warnings", () -> false,
        "Whether to allow moderators to remove warnings from themselves.");

    public static final ConfigNode<Boolean> ENABLE_NOTES = new ConfigNode<>(
        "moderation.notes.enable", () -> true,
        "Whether to enable the notes system. If disabled, the related commands are force-disabled.");
    public static final ConfigNode<Integer> MAX_NOTES_PER_MOD = new ConfigNode<>(
        "moderation.notes.max_amount", () -> Integer.MAX_VALUE,
        "The max amount of notes for a user per moderator.");

    GuildConfig(long guild, Path configPath) {
        this.guild = guild;
        this.config = CommentedFileConfig.builder(configPath, TomlFormat.instance())
            .onFileNotFound(FileNotFoundAction.CREATE_EMPTY)
            .preserveInsertionOrder()
            .autosave()
            .build();
        try {
            CONFIG.info("Building guild config for {} from {}", Long.toHexString(this.guild), configPath);
            config.load();
            FileWatcher.defaultInstance().addWatch(configPath, this::onFileChange);
            ConfigNode.nodes.forEach(this::forGuild);
            save();
        } catch (IOException ex) {
            JANITOR.error("Error while building config from file {}", configPath, ex);
        }
    }

    protected CommentedConfig getChannelOverrides() {
        final String channelOverridesID = "channel_overrides";
        CommentedConfig channelConfigs = config.get(channelOverridesID);
        if (channelConfigs == null) {
            channelConfigs = config.createSubConfig();
            config.set(channelOverridesID, channelConfigs);
            config.setComment(channelOverridesID, "Channel overrides for certain configuration options");
        }
        return channelConfigs;
    }

    public CommentedConfig getChannelConfig(GuildChannel channel) {
        final String id = channel.getId();
        CommentedConfig overrides = getChannelOverrides();
        CommentedConfig channelOverride = overrides.get(id);
        if (channelOverride == null) {
            channelOverride = overrides.createSubConfig();
            overrides.set(id, channelOverride);
            overrides.setComment(id, "Channel overrides for channel with name " + channel.getName());
        }
        return channelOverride;
    }

    private static void ensureComment(CommentedConfig config, String path, String expectedComment) {
        if (!Objects.equals(config.getComment(path), expectedComment)) {
            config.setComment(path, expectedComment);
        }
    }

    public <T> T forGuild(ConfigNode<T> node) {
        ensureComment(config, node.path, node.comment);
        T value = config.get(node.path);
        if (value == null) {
            value = node.defaultValue.get();
            config.set(node.path, value);
        }
        return value;
    }

    public <T> void forGuild(ConfigNode<T> node, T newValue) {
        ensureComment(config, node.path, node.comment);
        config.set(node.path, newValue);
    }

    public <T> T forChannel(GuildChannel channel, ConfigNode<T> node) {
        CommentedConfig channelConfig = getChannelConfig(channel);
        ensureComment(channelConfig, node.path, node.comment);
        T value = channelConfig.getRaw(node.path);
        if (value == null) {
            value = node.defaultValue.get();
            channelConfig.set(node.path, node.defaultValue.get());
        }
        return value;
    }

    public <T> void forChannel(GuildChannel channel, ConfigNode<T> node, T newValue) {
        CommentedConfig channelConfig = getChannelConfig(channel);
        ensureComment(channelConfig, node.path, node.comment);
        channelConfig.set(node.path, newValue);
    }

    public long getGuildID() {
        return guild;
    }

    public CommentedFileConfig getRawConfig() {
        return config;
    }

    public void save() {
        if (!closed) {
            config.save();
        }
    }

    public void close() {
        if (!closed) {
            closed = true;
            config.close();
        }
    }

    void onFileChange() {
        if (closed) return;
        try {
            CONFIG.info("Reloading config due to file change {}", config.getNioPath());
            config.load();
        } catch (Exception ex) {
            CONFIG.error("Error while reloading config from {}", config.getNioPath(), ex);
        }
    }

    public static class ConfigNode<T> {
        static final List<ConfigNode<?>> nodes = new ArrayList<>();

        public final String path;
        final String comment;
        final Supplier<T> defaultValue;

        ConfigNode(String path, Supplier<T> defaultValue, String... comment) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.comment = NEWLINE.join(comment);
            nodes.add(this);
        }
    }
}
