package sciwhiz12.janitor.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.javacord.api.event.message.MessageCreateEvent;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.utils.CommandHelper.literal;

public class PingCommand extends BaseCommand {
    private final String command;
    private final String reply;

    public PingCommand(CommandRegistry registry, String command, String reply) {
        super(registry);
        this.command = command;
        this.reply = reply;
    }

    public LiteralArgumentBuilder<MessageCreateEvent> getNode() {
        return literal(command)
            .executes(this::run);
    }

    int run(final CommandContext<MessageCreateEvent> ctx) {
        ctx.getSource()
            .getMessage()
            .getChannel()
            .sendMessage(reply)
            .whenCompleteAsync(Util.handle(
                success -> JANITOR.debug("Sent ping message to {}: {}", Util.toString(ctx.getSource().getMessageAuthor()), reply),
                err -> JANITOR.error("Error while sending ping message to {}", Util.toString(ctx.getSource().getMessageAuthor()))
                )
            );
        return 1;
    }
}
