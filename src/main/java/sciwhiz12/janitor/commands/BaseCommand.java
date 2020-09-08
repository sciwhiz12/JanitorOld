package sciwhiz12.janitor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import sciwhiz12.janitor.JanitorBot;

public abstract class BaseCommand {
    private final CommandRegistry registry;

    public BaseCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    public CommandRegistry getRegistry() {
        return registry;
    }

    public JanitorBot getBot() {
        return registry.getBot();
    }

    public abstract LiteralArgumentBuilder<MessageCreateEvent> getNode();
}
