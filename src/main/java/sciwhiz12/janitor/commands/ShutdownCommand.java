package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.JanitorBot;

public class ShutdownCommand implements ICommand {
    private final long ownerID;

    public ShutdownCommand(long ownerID) {
        this.ownerID = ownerID;
    }

    @Override
    public void onCommand(JanitorBot bot, MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == ownerID) {
            event.getMessage().getChannel().sendMessage("Shutting down. Goodbye!").queue();
            event.getJDA().shutdown();
        }
    }
}
