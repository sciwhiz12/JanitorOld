package sciwhiz12.janitor.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.javacord.api.event.message.MessageCreateEvent;
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

    public LiteralArgumentBuilder<MessageCreateEvent> getNode() {
        return literal("greet")
            .then(
                argument("user", UserArgument.user())
                    .executes(this::run)
            );
    }

    int run(final CommandContext<MessageCreateEvent> ctx) {
        UserArgument.getUser("user", ctx).getUsers(ctx.getSource().getApi())
            .thenCompose(user ->
                ctx.getSource()
                    .getMessage()
                    .getChannel()
                    .sendMessage("Hello " + user.getMentionTag() + " !")
            )
            .whenCompleteAsync(Util.handle(
                success -> JANITOR.debug("Sent greeting message to {}", Util.toString(ctx.getSource().getMessageAuthor())),
                err -> JANITOR.error("Error while sending greeting message to {}", Util.toString(ctx.getSource().getMessageAuthor()))
                )
            );
        return 1;
    }
}
