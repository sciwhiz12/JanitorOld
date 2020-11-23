package sciwhiz12.janitor.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import sciwhiz12.janitor.JanitorBotImpl;
import sciwhiz12.janitor.api.config.ConfigManager;
import sciwhiz12.janitor.api.config.ConfigNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static sciwhiz12.janitor.api.config.CoreConfigs.COMMAND_PREFIX;
import static sciwhiz12.janitor.api.config.CoreConfigs.ENABLE;

public class ConfigManagerImpl implements ConfigManager {
    private final JanitorBotImpl bot;
    private final Path configPath;
    private final Map<Long, GuildConfigImpl> configMap = new HashMap<>();
    private final List<ConfigNode<?>> configNodes = new ArrayList<>();

    public ConfigManagerImpl(JanitorBotImpl bot, Path configPath) {
        this.bot = bot;
        this.configPath = configPath;
        registerNodes(ENABLE, COMMAND_PREFIX);
    }

    @Override
    public GuildConfigImpl get(long guildID) {
        final GuildConfigImpl config = configMap.computeIfAbsent(guildID, (id) -> new GuildConfigImpl(id, getFile(guildID)));
        configNodes.forEach(config::forGuild); // Ensures the config is correct
        return config;
    }

    @Override
    public JanitorBotImpl getBot() {
        return bot;
    }

    @Override
    public void save() {
        configMap.values().forEach(GuildConfigImpl::save);
    }

    @Override
    public void registerNode(ConfigNode<?> node) {
        configNodes.add(node);
    }

    @Override
    public void close() {
        configMap.values().forEach(GuildConfigImpl::close);
        for (Iterator<GuildConfigImpl> iterator = configMap.values().iterator(); iterator.hasNext(); ) {
            iterator.next().close();
            iterator.remove();
        }
    }

    private Path getFile(long guildID) {
        final Path file = Path.of(Long.toHexString(guildID) + ".toml");
        return configPath.resolve(file);
    }

    static void ensureComment(CommentedConfig config, String path, String expectedComment) {
        if (!Objects.equals(config.getComment(path), expectedComment)) {
            config.setComment(path, expectedComment);
        }
    }
}
