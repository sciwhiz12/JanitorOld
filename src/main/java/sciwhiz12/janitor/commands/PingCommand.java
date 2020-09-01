package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand extends BaseCommand {
    public PingCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public void onCommand(MessageReceivedEvent event) {
        event.getMessage().getChannel().sendMessage("Pong!").queue();
    }
}
