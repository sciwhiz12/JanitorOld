package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.JanitorBot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandRegistry {
    private final JanitorBot bot;
    private final String prefix;
    private final Pattern pattern;
    private final Map<String, ICommand> registry = new HashMap<>();

    public CommandRegistry(JanitorBot bot, String prefix) {
        this.bot = bot;
        this.prefix = prefix;
        this.pattern = Pattern.compile("^" + prefix + "([A-Za-z0-9]+).*$");
    }

    public void addCommand(String cmd, ICommand instance) {
        registry.put(cmd, instance);
    }

    public void parseMessage(MessageReceivedEvent event) {
        try {
            String msg = event.getMessage().getContentStripped();
            Matcher matcher = pattern.matcher(msg);
            if (!matcher.matches()) { return; }
            String cmd = matcher.group(1);
            if (registry.containsKey(cmd)) {
                System.out.printf("Received command: %s ; full message: %s%n", cmd, msg);
                registry.get(cmd).onCommand(bot, event);
            }
        }
        catch (Exception e) {
            System.err
                    .printf("Error while parsing message: %s ; %s%n", event.getMessage().getContentStripped(),
                            e);
        }
    }
}
