package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class BaseCommand {
    protected final CommandRegistry registry;

    public BaseCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    public abstract void onCommand(MessageReceivedEvent event);
}
