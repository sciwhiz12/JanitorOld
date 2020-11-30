package sciwhiz12.janitor.commands.bot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.BaseCommand;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.api.Logging.JANITOR;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;

public class ShutdownCommand extends BaseCommand {
    public ShutdownCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("shutdown")
            .requires(ctx -> getBot().getBotConfig().getOwnerID().map(
                id -> id == ctx.getAuthor().getIdLong()).orElse(false)
            )
            .executes(this::run);
    }

    int run(final CommandContext<MessageReceivedEvent> ctx) {
        ctx.getSource()
            .getMessage()
            .getChannel()
            .sendMessage("Shutting down, in accordance with the owner's command. Goodbye all!")
            .reference(ctx.getSource().getMessage())
            .submit()
            .whenComplete(Util.handle(
                success -> JANITOR.debug("Sent shutdown message to channel {}", Util.toString(ctx.getSource().getAuthor())),
                err -> JANITOR
                    .error("Error while sending ping message to bot owner {}", Util.toString(ctx.getSource().getAuthor()))
                )
            )
            .join();
        getBot().shutdown();
        return 1;
    }
}
