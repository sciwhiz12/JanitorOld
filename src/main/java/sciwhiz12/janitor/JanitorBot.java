package sciwhiz12.janitor;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.STATUS;

public class JanitorBot {
    private final DiscordApi discord;
    private final BotConfig config;
    private final BotConsole console;
    private final CommandRegistry cmdRegistry;

    public JanitorBot(DiscordApi discord, BotConfig config) {
        this.config = config;
        this.console = new BotConsole(this, System.in);
        this.cmdRegistry = new CommandRegistry(this, config.getCommandPrefix());
        this.discord = discord;
        discord.addMessageCreateListener(cmdRegistry);
        discord.updateStatus(UserStatus.ONLINE);
        discord.updateActivity(ActivityType.PLAYING, " n' sweeping n' testing!");
        JANITOR.info("Ready!");
        config.getOwnerID()
            .map(ownerId -> getDiscord().getUserById(ownerId))
            .ifPresent(retrieveUser ->
                retrieveUser
                    .thenCompose(User::openPrivateChannel)
                    .thenCompose(channel -> channel.sendMessage("Started up and ready!"))
                    .whenCompleteAsync(Util.handle(
                        msg -> JANITOR.debug(STATUS, "Sent ready message to owner!"),
                        error -> JANITOR.error(STATUS, "Error while sending ready message to owner", error))
                    )
            );
        console.start();
    }

    public DiscordApi getDiscord() {
        return this.discord;
    }

    public BotConfig getConfig() {
        return this.config;
    }

    public CommandRegistry getCommandRegistry() {
        return this.cmdRegistry;
    }

    public void disconnect() {
        JANITOR.info(STATUS, "Shutting down!");
        console.stop();
        discord.disconnect();
//        getConfig().getOwnerID()
//            .map(id -> getJDA().getUserById(id))
//            .ifPresent(owner -> owner.openPrivateChannel().submit()
//                .thenCompose(channel -> channel.sendMessage(
//                    "Shutting down, in accordance with your orders. Goodbye!")
//                    .submit())
//                .whenComplete(Util.handle(
//                    msg -> JANITOR
//                        .debug(STATUS, "Sent shutdown message to owner: {}",
//                            Util.toString(owner)),
//                    err -> JANITOR
//                        .error(STATUS, "Error while sending shutdown message to owner", err)
//                ))
//                .join());
//        getJDA().shutdown();
    }
}
