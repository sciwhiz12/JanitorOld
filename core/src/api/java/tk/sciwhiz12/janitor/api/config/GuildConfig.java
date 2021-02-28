package tk.sciwhiz12.janitor.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.dv8tion.jda.api.entities.GuildChannel;

public interface GuildConfig {
    CommentedConfig getChannelOverrides();

    CommentedConfig getChannelConfig(GuildChannel channel);

    <T> T forGuild(ConfigNode<T> node);

    <T> void forGuild(ConfigNode<T> node, T newValue);

    <T> T forChannel(GuildChannel channel, ConfigNode<T> node);

    <T> void forChannel(GuildChannel channel, ConfigNode<T> node, T newValue);

    long getGuildID();

    CommentedConfig getRawConfig();

    void save();

    void close();
}
