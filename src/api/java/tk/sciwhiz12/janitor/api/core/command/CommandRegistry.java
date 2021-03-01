package tk.sciwhiz12.janitor.api.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tk.sciwhiz12.janitor.api.JanitorBot;

import java.util.function.Function;

public interface CommandRegistry {
    CommandDispatcher<MessageReceivedEvent> getDispatcher();

    void addCommand(Function<CommandRegistry, Command> command);

    JanitorBot getBot();
}
