package tk.sciwhiz12.janitor.api;

import net.dv8tion.jda.api.JDA;
import tk.sciwhiz12.janitor.api.core.command.CommandRegistry;
import tk.sciwhiz12.janitor.api.core.config.BotConfig;
import tk.sciwhiz12.janitor.api.core.config.ConfigManager;
import tk.sciwhiz12.janitor.api.core.messages.Messages;
import tk.sciwhiz12.janitor.api.core.messages.emote.ReactionManager;
import tk.sciwhiz12.janitor.api.core.messages.substitution.SubstitutionsMap;
import tk.sciwhiz12.janitor.api.core.module.ModuleManager;
import tk.sciwhiz12.janitor.api.core.storage.GuildStorageManager;

public interface JanitorBot {
    BotConfig getBotConfig();

    CommandRegistry getCommands();

    GuildStorageManager getGuildStorage();

    ConfigManager getConfigs();

    SubstitutionsMap getSubstitutions();

    ReactionManager getReactions();

    Messages getMessages();

    ModuleManager getModuleManager();

    void shutdown();

    JDA getDiscord();
}
