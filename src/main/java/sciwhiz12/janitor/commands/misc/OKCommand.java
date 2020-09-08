package sciwhiz12.janitor.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.javacord.api.event.message.MessageCreateEvent;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.utils.CommandHelper.literal;

public class OKCommand extends BaseCommand {
    public OKCommand(CommandRegistry registry) {
        super(registry);
    }

    public LiteralArgumentBuilder<MessageCreateEvent> getNode() {
        return literal("ok")
            .executes(this::run);
    }

    int run(final CommandContext<MessageCreateEvent> ctx) {
        ctx.getSource()
            .getMessage()
            .addReaction("\uD83D\uDC4C")
            .whenCompleteAsync(Util.handle(
                success -> JANITOR.debug("Reacted :ok_hand: to {}'s message", Util.toString(ctx.getSource().getMessageAuthor())),
                err -> JANITOR.error("Error while reacting :ok_hand: to {}'s message", Util.toString(ctx.getSource().getMessageAuthor()))
                )
            );
        return 1;
    }
}
