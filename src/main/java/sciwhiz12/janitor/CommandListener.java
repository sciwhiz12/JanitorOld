package sciwhiz12.janitor;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    public static final CommandListener INSTANCE = new CommandListener();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        JanitorBot.INSTANCE.getCommandRegistry().parseMessage(event);
    }
}
