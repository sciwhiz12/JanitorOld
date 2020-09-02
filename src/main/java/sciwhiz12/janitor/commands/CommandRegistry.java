package sciwhiz12.janitor.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.commands.bot.ShutdownCommand;
import sciwhiz12.janitor.commands.misc.OKCommand;
import sciwhiz12.janitor.commands.misc.PingCommand;
import sciwhiz12.janitor.listeners.BaseListener;
import sciwhiz12.janitor.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static sciwhiz12.janitor.Logging.COMMANDS;
import static sciwhiz12.janitor.Logging.JANITOR;

public class CommandRegistry extends BaseListener {
    private final Pattern pattern;
    private final String prefix;
    private final Map<String, BaseCommand> registry = new HashMap<>();
    private final CommandDispatcher<MessageReceivedEvent> dispatcher;

    public CommandRegistry(JanitorBot bot, String prefix) {
        super(bot);
        this.pattern = Pattern.compile("^" + prefix + "([A-Za-z0-9]+).*$");
        this.prefix = prefix;
        this.dispatcher = new CommandDispatcher<>();

        addCommand(new PingCommand(this, "ping", "Pong!"));
        addCommand(new PingCommand(this, "pong", "Ping!"));
        addCommand(new OKCommand(this));
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
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        if (!msg.startsWith(this.prefix)) return;
        try {
            StringReader command = new StringReader(msg.substring(this.prefix.length()));
            ParseResults<MessageReceivedEvent> parseResults = this.dispatcher.parse(command, event);
            if (parseResults.getReader().canRead()) {
                // Parsing did not succeed, i.e. command not found
                // TODO: add separate code path when insufficient permissions / requires fails
                return;
            }
            JANITOR.debug(COMMANDS, "Received command and executing. Author: {}, full message: {}", Util.toString(event.getAuthor()), msg);
            dispatcher.execute(parseResults);
        } catch (CommandSyntaxException ex) {
            JANITOR.error(COMMANDS, "Error while parsing message and executing command", ex);
        }
    }
}
