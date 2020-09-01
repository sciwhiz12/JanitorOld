package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.STATUS;

public class ShutdownCommand extends BaseCommand {
    private final long ownerID;

    public ShutdownCommand(CommandRegistry registry, long ownerID) {
        super(registry);
        System.out.println(ownerID);
        this.ownerID = ownerID;
    }

    @Override
    public void onCommand(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == ownerID) {
            event.getMessage().getChannel()
                .sendMessage("Shutting down, in accordance with the owner's command. Goodbye all!")
                .queue();
            registry.getBot().getConfig().getOwnerID()
                .map(id -> event.getJDA().getUserById(id))
                .ifPresent(owner -> owner.openPrivateChannel().submit()
                    .thenCompose(channel -> channel.sendMessage(
                        "Shutting down, in accordance with your orders. Goodbye!")
                        .submit())
                    .whenComplete(Util.handle(
                        msg -> JANITOR
                            .debug(STATUS, "Sent shutdown message to owner: {}",
                                Util.toString(owner)),
                        err -> JANITOR
                            .error(STATUS, "Error while sending shutdown message to owner", err)
                    ))
                    .thenAccept(v -> registry.getBot().shutdown())
                    .join());
        }
    }
}
