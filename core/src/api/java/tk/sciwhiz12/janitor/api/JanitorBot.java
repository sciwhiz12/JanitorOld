package tk.sciwhiz12.janitor.api;

import net.dv8tion.jda.api.JDA;
import tk.sciwhiz12.janitor.api.command.CommandRegistry;
import tk.sciwhiz12.janitor.api.config.BotConfig;
import tk.sciwhiz12.janitor.api.config.ConfigManager;
import tk.sciwhiz12.janitor.api.messages.Messages;
import tk.sciwhiz12.janitor.api.messages.emote.ReactionManager;
import tk.sciwhiz12.janitor.api.messages.substitution.SubstitutionsMap;
import tk.sciwhiz12.janitor.api.module.ModuleManager;
import tk.sciwhiz12.janitor.api.storage.GuildStorageManager;

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
