package sciwhiz12.janitor.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.BaseCommand;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.api.Logging.JANITOR;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;

public class PingCommand extends BaseCommand {
    private final String command;
    private final String reply;

    public PingCommand(CommandRegistry registry, String command, String reply) {
        super(registry);
        this.command = command;
        this.reply = reply;
    }

    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal(command)
            .executes(this::run);
    }

    int run(final CommandContext<MessageReceivedEvent> ctx) {
        ctx.getSource()
            .getMessage()
            .getChannel()
            .sendMessage(reply)
            .reference(ctx.getSource().getMessage())
            .queue(
                success -> JANITOR.debug("Sent ping message to {}: {}", Util.toString(ctx.getSource().getAuthor()), reply),
                err -> JANITOR.error("Error while sending ping message to {}", Util.toString(ctx.getSource().getAuthor()))
            );
        return 1;
    }
}
