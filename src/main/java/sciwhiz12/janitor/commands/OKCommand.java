package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OKCommand extends BaseCommand {
    public OKCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public void onCommand(MessageReceivedEvent event) {
        event.getMessage().addReaction("\uD83D\uDC4C").queue();
    }
}
