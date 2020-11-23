package sciwhiz12.janitor.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.BaseCommand;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.api.Logging.JANITOR;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;

public class OKCommand extends BaseCommand {
    public OKCommand(CommandRegistry registry) {
        super(registry);
    }

    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("ok")
            .executes(this::run);
    }

    int run(final CommandContext<MessageReceivedEvent> ctx) {
        ctx.getSource()
            .getMessage()
            .addReaction("\uD83D\uDC4C")
            .queue(
                success -> JANITOR.debug("Reacted :ok_hand: to {}'s message", Util.toString(ctx.getSource().getAuthor())),
                err -> JANITOR
                    .error("Error while reacting :ok_hand: to {}'s message", Util.toString(ctx.getSource().getAuthor()))
            );
        return 1;
    }
}
