package tk.sciwhiz12.janitor.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Joiner;
import net.dv8tion.jda.api.entities.GuildChannel;
import tk.sciwhiz12.janitor.api.config.ConfigNode;
import tk.sciwhiz12.janitor.api.config.GuildConfig;

import java.io.IOException;
import java.nio.file.Path;

import static tk.sciwhiz12.janitor.api.Logging.CONFIG;
import static tk.sciwhiz12.janitor.api.Logging.JANITOR;
import static tk.sciwhiz12.janitor.config.ConfigManagerImpl.ensureComment;

public class GuildConfigImpl implements GuildConfig {
    private static final Joiner NEWLINE = Joiner.on("\n");

    private final long guild;
    private final CommentedFileConfig config;
    private boolean closed = false;

    GuildConfigImpl(long guild, Path configPath) {
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
            //            ConfigNode.nodes.forEach(this::forGuild);
            save();
        } catch (IOException ex) {
            JANITOR.error("Error while building config from file {}", configPath, ex);
        }
    }

    @Override
    public CommentedConfig getChannelOverrides() {
        final String channelOverridesID = "channel_overrides";
        CommentedConfig channelConfigs = config.get(channelOverridesID);
        if (channelConfigs == null) {
            channelConfigs = config.createSubConfig();
            config.set(channelOverridesID, channelConfigs);
            config.setComment(channelOverridesID, "Channel overrides for certain configuration options");
        }
        return channelConfigs;
    }

    @Override
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

    @Override
    public <T> T forGuild(ConfigNode<T> node) {
        ensureComment(config, node.path(), node.comment());
        T value = config.get(node.path());
        if (value == null) {
            value = node.defaultValue().get();
            config.set(node.path(), value);
        }
        return value;
    }

    @Override
    public <T> void forGuild(ConfigNode<T> node, T newValue) {
        ensureComment(config, node.path(), node.comment());
        config.set(node.path(), newValue);
    }

    @Override
    public <T> T forChannel(GuildChannel channel, ConfigNode<T> node) {
        CommentedConfig channelConfig = getChannelConfig(channel);
        ensureComment(channelConfig, node.path(), node.comment());
        T value = channelConfig.getRaw(node.path());
        if (value == null) {
            value = node.defaultValue().get();
            channelConfig.set(node.path(), node.defaultValue().get());
        }
        return value;
    }

    @Override
    public <T> void forChannel(GuildChannel channel, ConfigNode<T> node, T newValue) {
        CommentedConfig channelConfig = getChannelConfig(channel);
        ensureComment(channelConfig, node.path(), node.comment());
        channelConfig.set(node.path(), newValue);
    }

    @Override
    public long getGuildID() {
        return guild;
    }

    @Override
    public CommentedFileConfig getRawConfig() {
        return config;
    }

    @Override
    public void save() {
        if (!closed) {
            config.save();
        }
    }

    @Override
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
}
