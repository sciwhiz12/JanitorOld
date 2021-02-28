package tk.sciwhiz12.janitor.api.config;

import tk.sciwhiz12.janitor.api.JanitorBot;

public interface ConfigManager {
    GuildConfig get(long guildID);

    void save();

    void close();

    void registerNode(ConfigNode<?> node);

    default void registerNodes(ConfigNode<?>... nodes) {
        for (ConfigNode<?> node : nodes) {
            registerNode(node);
        }
    }

    JanitorBot getBot();
}
