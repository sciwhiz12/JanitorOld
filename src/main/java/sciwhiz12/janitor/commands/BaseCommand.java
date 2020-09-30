package sciwhiz12.janitor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.config.BotConfig;
import sciwhiz12.janitor.msg.Messages;

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

    protected Messages messages() {
        return getBot().getMessages();
    }

    protected BotConfig config() {
        return getBot().getConfig();
    }

    public abstract LiteralArgumentBuilder<MessageReceivedEvent> getNode();
}
