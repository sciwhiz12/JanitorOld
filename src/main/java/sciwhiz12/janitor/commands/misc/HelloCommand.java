package sciwhiz12.janitor.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.commands.arguments.UserArgument;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.utils.CommandHelper.argument;
import static sciwhiz12.janitor.utils.CommandHelper.literal;

public class HelloCommand extends BaseCommand {
    public HelloCommand(CommandRegistry registry) {
        super(registry);
    }

    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("greet")
            .then(
                argument("user", UserArgument.user())
                    .executes(this::run)
            );
    }

    int run(final CommandContext<MessageReceivedEvent> ctx) {
        UserArgument.getUser("user", ctx)
            .getUsers(getBot().getDiscord())
            .flatMap(user -> ctx.getSource().getChannel().sendMessage("Hello " + user.getAsMention() + "!"))
            .queue(
                success -> JANITOR.debug("Sent greeting message to {}", Util.toString(ctx.getSource().getAuthor())),
                err -> JANITOR.error("Error while sending greeting message to {}", Util.toString(ctx.getSource().getAuthor()))
            );
        return 1;
    }
}
