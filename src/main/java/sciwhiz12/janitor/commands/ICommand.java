package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.JanitorBot;

public interface ICommand {
    void onCommand(JanitorBot bot, MessageReceivedEvent event);
}
