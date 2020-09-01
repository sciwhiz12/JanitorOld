package sciwhiz12.janitor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.listeners.StatusListener;

import javax.security.auth.login.LoginException;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.STATUS;

public class JanitorBot {
    private final JDA jda;
    private final BotConfig config;
    private final CommandRegistry cmdRegistry;

    public JanitorBot(JDABuilder jdaBuilder, BotConfig config) throws LoginException {
        this.config = config;
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
        getJDA().shutdown();
    }
}
