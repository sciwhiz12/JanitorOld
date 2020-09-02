package sciwhiz12.janitor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.listeners.StatusListener;
import sciwhiz12.janitor.utils.Util;

import javax.security.auth.login.LoginException;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.STATUS;

public class JanitorBot {
    private final JDA jda;
    private final BotConfig config;
    private final BotConsole console;
    private final CommandRegistry cmdRegistry;

    public JanitorBot(JDABuilder jdaBuilder, BotConfig config) throws LoginException {
        this.config = config;
        this.console = new BotConsole(this, System.in);
        this.cmdRegistry = new CommandRegistry(this, config.getCommandPrefix());
        jdaBuilder
            .setActivity(Activity.playing("the Readying game..."))
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .setAutoReconnect(true)
            .addEventListeners(
                cmdRegistry,
                new StatusListener(this)
            );
        this.jda = jdaBuilder.build();
        JANITOR.info(STATUS, "Bot is built");
//        console.start();
    }

    public JDA getJDA() {
        return this.jda;
    }

    public BotConfig getConfig() {
        return this.config;
    }

    public CommandRegistry getCommandRegistry() {
        return this.cmdRegistry;
    }

    public void shutdown() {
        JANITOR.info(STATUS, "Shutting down!");
        console.stop();
        getJDA().getRegisteredListeners().forEach(listener -> getJDA().removeEventListener(listener));
        getConfig().getOwnerID()
            .map(id -> getJDA().getUserById(id))
            .ifPresent(owner -> owner.openPrivateChannel().submit()
                .thenCompose(channel -> channel.sendMessage(
                    "Shutting down, in accordance with your orders. Goodbye!")
                    .submit())
                .whenComplete(Util.handle(
                    msg -> JANITOR
                        .debug(STATUS, "Sent shutdown message to owner: {}",
                            Util.toString(owner)),
                    err -> JANITOR
                        .error(STATUS, "Error while sending shutdown message to owner", err)
                ))
                .join());
        getJDA().shutdown();
    }
}
