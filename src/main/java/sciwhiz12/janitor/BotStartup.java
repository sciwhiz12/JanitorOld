package sciwhiz12.janitor;

import org.javacord.api.DiscordApiBuilder;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.config.BotOptions;

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
            DiscordApiBuilder builder = new DiscordApiBuilder().setToken(config.getToken().get());
            builder.login()
                .thenAccept(api -> new JanitorBot(api, config));
        } catch (Exception ex) {
            JANITOR.error("Error while building Discord connection", ex);
        }
    }
}
