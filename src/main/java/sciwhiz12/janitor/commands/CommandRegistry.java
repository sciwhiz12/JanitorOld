package sciwhiz12.janitor.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.commands.bot.ShutdownCommand;
import sciwhiz12.janitor.commands.misc.HelloCommand;
import sciwhiz12.janitor.commands.misc.OKCommand;
import sciwhiz12.janitor.commands.misc.PingCommand;
import sciwhiz12.janitor.utils.Util;

import java.util.HashMap;
import java.util.Map;

import static sciwhiz12.janitor.Logging.COMMANDS;
import static sciwhiz12.janitor.Logging.JANITOR;

public class CommandRegistry implements EventListener {
    private final JanitorBot bot;
    private final String prefix;
    private final Map<String, BaseCommand> registry = new HashMap<>();
    private final CommandDispatcher<MessageReceivedEvent> dispatcher;

    public CommandRegistry(JanitorBot bot, String prefix) {
        this.bot = bot;
        this.prefix = prefix;
        this.dispatcher = new CommandDispatcher<>();

        addCommand(new PingCommand(this, "ping", "Pong!"));
        addCommand(new PingCommand(this, "pong", "Ping!"));
        addCommand(new OKCommand(this));
        addCommand(new HelloCommand(this));
        if (bot.getConfig().getOwnerID().isPresent()) {
            addCommand(new ShutdownCommand(this, bot.getConfig().getOwnerID().get()));
        }
    }

    public CommandDispatcher<MessageReceivedEvent> getDispatcher() {
        return this.dispatcher;
    }

    public JanitorBot getBot() {
        return this.bot;
    }

    public void addCommand(BaseCommand instance) {
        dispatcher.register(instance.getNode());
    }

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (!(genericEvent instanceof MessageReceivedEvent)) return;
        MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
        String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(this.prefix)) return;
        JANITOR.debug(COMMANDS, "Received message starting with valid command prefix. Author: {}, full message: {}",
            Util.toString(event.getAuthor()), msg);
        try {
            StringReader command = new StringReader(msg.substring(this.prefix.length()));
            ParseResults<MessageReceivedEvent> parseResults = this.dispatcher.parse(command, event);
            if (parseResults.getReader().canRead()) {
                // Parsing did not succeed, i.e. command not found
                // TODO: add separate code path when insufficient permissions / requires fails
                JANITOR.error(COMMANDS, "Error while parsing command: {}", parseResults.getExceptions().values());
                return;
            }
            JANITOR.debug(COMMANDS, "Executing command.");
            dispatcher.execute(parseResults);
        } catch (CommandSyntaxException ex) {
            JANITOR.error(COMMANDS, "Error while parsing message and executing command", ex);
        }
    }
}
