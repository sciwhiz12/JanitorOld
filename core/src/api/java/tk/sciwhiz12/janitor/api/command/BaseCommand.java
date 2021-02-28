package tk.sciwhiz12.janitor.api.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tk.sciwhiz12.janitor.api.JanitorBot;

public abstract class BaseCommand implements Command {
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

    public abstract LiteralArgumentBuilder<MessageReceivedEvent> getNode();
}
