package tk.sciwhiz12.janitor;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import tk.sciwhiz12.janitor.config.BotConfigImpl;
import tk.sciwhiz12.janitor.config.BotOptions;

import java.util.EnumSet;

import static tk.sciwhiz12.janitor.api.Logging.JANITOR;

public class BotStartup {
    public static void main(String[] args) {
        JANITOR.info("Starting...");

        BotOptions options = new BotOptions(args);
        BotConfigImpl config = new BotConfigImpl(options);

        JANITOR.info("Building bot instance and connecting to Discord...");

        try {
            JDABuilder.create(config.getToken(), EnumSet.allOf(GatewayIntent.class))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("for the ready call..."))
                .setAutoReconnect(true)
                .addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onReady(@NotNull ReadyEvent event) {
                        new JanitorBotImpl(event.getJDA(), config);
                    }
                })
                .build();
        } catch (Exception ex) {
            JANITOR.error("Error while building Discord connection", ex);
            System.exit(1);
        }
    }
}
