package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand extends BaseCommand {
    private final String message;

    public PingCommand(CommandRegistry registry, String message) {
        super(registry);
        this.message = message;
    }

    @Override
    public void onCommand(MessageReceivedEvent event) {
        event.getMessage().getChannel().sendMessage(message).queue();
    }
}
