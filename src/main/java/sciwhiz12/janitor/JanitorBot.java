package sciwhiz12.janitor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.msg.Messages;
import sciwhiz12.janitor.msg.Translations;
import sciwhiz12.janitor.utils.Util;

import java.util.concurrent.CompletableFuture;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.STATUS;

public class JanitorBot {
    private final JDA discord;
    private final BotConfig config;
    private final Messages messages;
    private BotConsole console;
    private CommandRegistry cmdRegistry;
    private Translations translations;

    public JanitorBot(JDA discord, BotConfig config) {
        this.config = config;
        this.console = new BotConsole(this, System.in);
        this.cmdRegistry = new CommandRegistry(this, config.getCommandPrefix());
        this.discord = discord;
        this.translations = new Translations(this, config.getTranslationsFile());
        this.messages = new Messages(this);
        discord.addEventListener(cmdRegistry);
        discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(" n' sweeping n' testing!"));
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
        console.start();
    }

    public JDA getDiscord() {
        return this.discord;
    }

    public BotConfig getConfig() {
        return this.config;
    }

    public Messages getMessages() { return this.messages; }

    public CommandRegistry getCommandRegistry() {
        return this.cmdRegistry;
    }

    public Translations getTranslations() {
        return this.translations;
    }

    public void shutdown() {
        JANITOR.info(STATUS, "Shutting down!");
        console.stop();
        getConfig().getOwnerID()
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
        discord.shutdown();
    }
}
