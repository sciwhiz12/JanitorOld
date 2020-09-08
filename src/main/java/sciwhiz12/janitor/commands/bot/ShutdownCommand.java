package sciwhiz12.janitor.commands.bot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.javacord.api.event.message.MessageCreateEvent;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.utils.CommandHelper.literal;

public class ShutdownCommand extends BaseCommand {
    private final long ownerID;

    public ShutdownCommand(CommandRegistry registry, long ownerID) {
        super(registry);
        this.ownerID = ownerID;
    }

    @Override
    public LiteralArgumentBuilder<MessageCreateEvent> getNode() {
        return literal("shutdown")
            .requires(ctx -> ctx.getMessageAuthor().getId() == ownerID)
            .executes(this::run);
    }

    int run(final CommandContext<MessageCreateEvent> ctx) {
        ctx.getSource()
            .getMessage()
            .getChannel()
            .sendMessage("Shutting down, in accordance with the owner's command. Goodbye all!")
            .whenCompleteAsync(Util.handle(
                success -> JANITOR.debug("Sent shutdown message to channel {}", Util.toString(ctx.getSource().getMessageAuthor())),
                err -> JANITOR.error("Error while sending ping message to bot owner {}", Util.toString(ctx.getSource().getMessageAuthor()))
                )
            ).join();
        getBot().disconnect();
        return 1;
    }
}
