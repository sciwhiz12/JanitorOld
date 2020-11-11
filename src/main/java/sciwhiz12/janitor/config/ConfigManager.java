package sciwhiz12.janitor.config;

import sciwhiz12.janitor.JanitorBot;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConfigManager {
    private final JanitorBot bot;
    private final Path configPath;
    private final Map<Long, GuildConfig> configMap = new HashMap<>();

    public ConfigManager(JanitorBot bot, Path configPath) {
        this.bot = bot;
        this.configPath = configPath;
    }

    public GuildConfig getConfig(long guildID) {
        return configMap.computeIfAbsent(guildID, (id) -> new GuildConfig(id, getFile(guildID)));
    }

    public JanitorBot getBot() {
        return bot;
    }

    public void save() {
        configMap.values().forEach(GuildConfig::save);
    }

    public void close() {
        configMap.values().forEach(GuildConfig::close);
        for (Iterator<GuildConfig> iterator = configMap.values().iterator(); iterator.hasNext(); ) {
            iterator.next().close();
            iterator.remove();
        }
    }

    private Path getFile(long guildID) {
        final Path file = Path.of(Long.toHexString(guildID) + ".toml");
        return configPath.resolve(file);
    }
}
