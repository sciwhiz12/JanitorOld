package tk.sciwhiz12.janitor.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import tk.sciwhiz12.janitor.JanitorBotImpl;
import tk.sciwhiz12.janitor.api.command.Command;
import tk.sciwhiz12.janitor.api.command.CommandRegistry;
import tk.sciwhiz12.janitor.api.config.CoreConfigs;
import tk.sciwhiz12.janitor.commands.bot.AboutCommand;
import tk.sciwhiz12.janitor.commands.bot.ShutdownCommand;
import tk.sciwhiz12.janitor.commands.misc.HelloCommand;
import tk.sciwhiz12.janitor.commands.misc.OKCommand;
import tk.sciwhiz12.janitor.commands.misc.PingCommand;
import tk.sciwhiz12.janitor.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static tk.sciwhiz12.janitor.api.Logging.COMMANDS;
import static tk.sciwhiz12.janitor.api.Logging.JANITOR;

public class CommandRegistryImpl implements CommandRegistry, EventListener {
    private final JanitorBotImpl bot;
    final Map<String, Command> registry = new HashMap<>();
    private final CommandDispatcher<MessageReceivedEvent> dispatcher;

    public CommandRegistryImpl(JanitorBotImpl bot) {
        this.bot = bot;
        this.dispatcher = new CommandDispatcher<>();

        addCommand(reg -> new PingCommand(reg, "ping", "Pong!"));
        addCommand(reg -> new PingCommand(reg, "pong", "Ping!"));
        addCommand(OKCommand::new);
        addCommand(HelloCommand::new);
        addCommand(ShutdownCommand::new);
        addCommand(AboutCommand::new);
        addCommand((reg) -> new CmdListCommand((CommandRegistryImpl) reg));
    }

    @Override
    public CommandDispatcher<MessageReceivedEvent> getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public JanitorBotImpl getBot() {
        return this.bot;
    }

    @Override
    public void addCommand(Function<CommandRegistry, Command> commandCreate) {
        final Command command = commandCreate.apply(this);
        final LiteralArgumentBuilder<MessageReceivedEvent> node = command.getNode();
        final String literal = node.getLiteral();
        if (registry.containsKey(literal)) {
            throw new IllegalArgumentException("Command " + literal + " already exists");
        }
        dispatcher.register(node);
        registry.put(literal, command);
    }

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (!(genericEvent instanceof MessageReceivedEvent)) return;
        MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
        if (event.getAuthor().isBot()) return;
        final String prefix;
        if (event.isFromGuild()) {
            prefix = getBot().getConfigs().get(event.getGuild().getIdLong())
                .forGuild(CoreConfigs.COMMAND_PREFIX);
        } else {
            prefix = getBot().getBotConfig().getCommandPrefix();
        }

        String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(prefix)) return;
        JANITOR.debug(COMMANDS, "Received message starting with valid command prefix. Author: {}, full message: {}",
            Util.toString(event.getAuthor()), msg);
        try {
            StringReader command = new StringReader(msg.substring(prefix.length()));
            ParseResults<MessageReceivedEvent> parseResults = this.dispatcher.parse(command, event);
            if (parseResults.getReader().canRead()) {
                if (parseResults.getExceptions().isEmpty()) {
                    JANITOR.info(COMMANDS, "Command not found.");
                } else {
                    JANITOR.error(COMMANDS, "Error while parsing command: {}", parseResults.getExceptions().values());
                }
                return;
            }
            JANITOR.debug(COMMANDS, "Executing command.");
            dispatcher.execute(parseResults);
        } catch (CommandSyntaxException ex) {
            JANITOR.error(COMMANDS, "Error while parsing message and executing command", ex);
        }
    }
}
