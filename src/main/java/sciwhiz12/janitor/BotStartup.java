package sciwhiz12.janitor;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.config.BotOptions;

import java.util.EnumSet;

import static com.google.common.base.Preconditions.checkArgument;
import static sciwhiz12.janitor.Logging.JANITOR;

public class BotStartup {
    public static void main(String[] args) {
        JANITOR.info("Starting...");

        BotOptions options = new BotOptions(args);
        BotConfig config = new BotConfig(options);
        checkArgument(config.getToken().isPresent(),
            "Token is not supplied through config or command line");

        JANITOR.info("Building bot instance and connecting to Discord...");

        try {
            JDABuilder.create(config.getToken().get(), EnumSet.allOf(GatewayIntent.class))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("for the ready call..."))
                .setAutoReconnect(true)
                .addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onReady(@NotNull ReadyEvent event) {
                        new JanitorBot(event.getJDA(), config);
                    }
                })
                .build();
        } catch (Exception ex) {
            JANITOR.error("Error while building Discord connection", ex);
        }
    }
}
