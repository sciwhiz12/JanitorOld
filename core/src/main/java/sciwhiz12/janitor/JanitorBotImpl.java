package sciwhiz12.janitor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import sciwhiz12.janitor.api.JanitorBot;
import sciwhiz12.janitor.commands.CommandRegistryImpl;
import sciwhiz12.janitor.config.BotConfigImpl;
import sciwhiz12.janitor.config.ConfigManagerImpl;
import sciwhiz12.janitor.messages.MessagesImpl;
import sciwhiz12.janitor.messages.emote.ReactionManagerImpl;
import sciwhiz12.janitor.messages.substitution.SubstitutionsMapImpl;
import sciwhiz12.janitor.module.ModuleManagerImpl;
import sciwhiz12.janitor.storage.GuildStorageManagerImpl;
import sciwhiz12.janitor.utils.Util;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static sciwhiz12.janitor.api.Logging.JANITOR;
import static sciwhiz12.janitor.api.Logging.STATUS;

public class JanitorBotImpl implements JanitorBot {
    private final JDA discord;
    private final BotConfigImpl config;
    private final BotConsole console;
    private final GuildStorageManagerImpl storage;
    private final GuildStorageManagerImpl.SavingThread storageSavingThread;
    private final ConfigManagerImpl configManager;
    private final CommandRegistryImpl cmdRegistry;
    private final SubstitutionsMapImpl substitutions;
    private final MessagesImpl messages;
    private final ReactionManagerImpl reactions;
    private final ModuleManagerImpl modules;

    public JanitorBotImpl(JDA discord, BotConfigImpl config) {
        this.config = config;
        this.discord = discord;
        this.console = new BotConsole(this, System.in);
        this.storage = new GuildStorageManagerImpl(this, Path.of(config.STORAGE_PATH.get()));
        this.configManager = new ConfigManagerImpl(this, config.getConfigsFolder());
        this.cmdRegistry = new CommandRegistryImpl(this);
        this.substitutions = new SubstitutionsMapImpl(this);
        this.messages = new MessagesImpl(this);
        this.reactions = new ReactionManagerImpl(this);
        this.modules = new ModuleManagerImpl(this);
        modules.activateModules();
        // TODO: find which of these can be loaded in parallel before the bot JDA is ready
        discord.addEventListener(cmdRegistry, reactions);
        discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(" n' sweeping n' testing!"));
        //noinspection ResultOfMethodCallIgnored
        discord.getGuilds().forEach(Guild::loadMembers);
        JANITOR.info("Ready!");
        config.getOwnerID()
            .map(discord::retrieveUserById)
            .ifPresent(retrieveUser ->
                retrieveUser
                    .flatMap(User::openPrivateChannel)
                    .flatMap(channel -> channel.sendMessage("Started up and ready!"))
                    .queue(
                        msg -> JANITOR.debug(STATUS, "Sent ready message to owner!"),
                        error -> JANITOR.error(STATUS, "Error while sending ready message to owner", error)
                    )
            );
        storageSavingThread = new GuildStorageManagerImpl.SavingThread(storage);
        storageSavingThread.start();
        console.start();
    }

    @Override
    public JDA getDiscord() {
        return this.discord;
    }

    @Override
    public BotConfigImpl getBotConfig() {
        return this.config;
    }

    @Override
    public MessagesImpl getMessages() {
        return messages;
    }

    @Override
    public SubstitutionsMapImpl getSubstitutions() {
        return substitutions;
    }

    @Override
    public GuildStorageManagerImpl getGuildStorage() {
        return this.storage;
    }

    @Override
    public ConfigManagerImpl getConfigs() {
        return configManager;
    }

    @Override
    public CommandRegistryImpl getCommands() {
        return this.cmdRegistry;
    }

    @Override
    public ReactionManagerImpl getReactions() {
        return this.reactions;
    }

    @Override
    public ModuleManagerImpl getModuleManager() {
        return modules;
    }

    @Override
    public void shutdown() {
        JANITOR.info(STATUS, "Shutting down!");
        getBotConfig().getOwnerID()
            .map(discord::retrieveUserById)
            .map(owner ->
                owner
                    .flatMap(User::openPrivateChannel)
                    .flatMap(channel ->
                        channel.sendMessage("Shutting down, in accordance with your orders. Goodbye!"))
                    .submit()
                    .whenComplete(Util.handle(
                        msg ->
                            JANITOR
                                .debug(STATUS, "Sent shutdown message to owner: {}",
                                    Util.toString(((PrivateChannel) msg.getChannel()).getUser())),
                        err ->
                            JANITOR
                                .error(STATUS, "Error while sending shutdown message to owner", err)
                    ))
            ).ifPresent(CompletableFuture::join);
        modules.shutdown();
        discord.shutdown();
        storageSavingThread.stopThread();
        storage.save();
        configManager.save();
        configManager.close();
        console.stop();
        config.close();
    }
}
