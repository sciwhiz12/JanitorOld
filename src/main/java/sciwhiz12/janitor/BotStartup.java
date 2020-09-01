package sciwhiz12.janitor;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.config.BotOptions;

import static sciwhiz12.janitor.Logging.JANITOR;

public class BotStartup {
    public static void main(String[] args) {
        JANITOR.info("Starting...");

        BotOptions options = new BotOptions(args);
        BotConfig config = new BotConfig(options);

        JANITOR.info("Building bot instance and connecting to Discord...");

        JDABuilder builder;
        JanitorBot bot;
        try {
            Preconditions.checkArgument(config.getToken().isPresent(),
                "Token is not supplied through config or command line");
            builder = JDABuilder.createDefault(config.getToken().get());
            bot = new JanitorBot(builder, config);

            bot.getJDA().awaitReady();
            String inviteURL = bot.getJDA().getInviteUrl(Permission.ADMINISTRATOR);
            JANITOR.info("Invite URL (gives ADMIN permission): " + inviteURL);
        } catch (Exception ex) {
            JANITOR.error("Error while building Discord connection", ex);
        }
    }
}
