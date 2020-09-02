package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShutdownCommand extends BaseCommand {
    private final long ownerID;

    public ShutdownCommand(CommandRegistry registry, long ownerID) {
        super(registry);
        this.ownerID = ownerID;
    }

    @Override
    public void onCommand(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == ownerID) {
            event.getMessage().getChannel()
                .sendMessage("Shutting down, in accordance with the owner's command. Goodbye all!")
                .complete();
            registry.getBot().shutdown();
        }
    }
}
