package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.JanitorBot;

public class PingCommand implements ICommand {
    @Override
    public void onCommand(JanitorBot bot, MessageReceivedEvent event) {
        event.getMessage().getChannel().sendMessage("Pong!").queue();
    }
}
