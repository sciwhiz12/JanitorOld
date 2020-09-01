package sciwhiz12.janitor;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.commands.OKCommand;
import sciwhiz12.janitor.commands.PingCommand;
import sciwhiz12.janitor.commands.ShutdownCommand;

import javax.security.auth.login.LoginException;

public class JanitorBot {
    public static JanitorBot INSTANCE;

    private final JDA jda;
    private final CommandRegistry cmdRegistry;

    public JanitorBot(JDA jda, String prefix) {
        this.jda = jda;
        this.cmdRegistry = new CommandRegistry(this, prefix);
    }

    public JDA getDiscord() {
        return this.jda;
    }

    public CommandRegistry getCommandRegistry() {
        return this.cmdRegistry;
    }

    public static void main(String[] args) throws LoginException {
        System.out.println("Starting...");

        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<String> token = parser
                .accepts("token", "The Discord token for the bot user").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<String> prefix = parser
                .accepts("prefix", "The prefix for commands").withRequiredArg().defaultsTo("!");
        ArgumentAcceptingOptionSpec<Long> owner = parser.accepts("owner",
                "The snowflake ID of the bot owner; Used for shutdowns and other bot management commands")
                .withRequiredArg().ofType(Long.class);

        OptionSet options = parser.parse(args);

        System.out.println("Configuring and connecting...");

        JDABuilder builder = JDABuilder.createDefault(token.value(options));
        builder.addEventListeners(CommandListener.INSTANCE);
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS);
        JDA jda = builder.build();
        INSTANCE = new JanitorBot(jda, prefix.value(options));

        String inviteURL = jda.getInviteUrl(Permission.ADMINISTRATOR);

        INSTANCE.getCommandRegistry().addCommand("ping", new PingCommand());
        INSTANCE.getCommandRegistry().addCommand("ok", new OKCommand());
        if (options.has(owner)) {
            INSTANCE.getCommandRegistry().addCommand("shutdown", new ShutdownCommand(owner.value(options)));
        }

        System.out.println("Ready! Invite URL: " + inviteURL);
    }
}
