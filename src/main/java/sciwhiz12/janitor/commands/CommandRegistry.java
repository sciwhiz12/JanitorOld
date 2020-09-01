package sciwhiz12.janitor.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.listeners.BaseListener;
import sciwhiz12.janitor.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sciwhiz12.janitor.Logging.COMMANDS;
import static sciwhiz12.janitor.Logging.JANITOR;

public class CommandRegistry extends BaseListener {
    private final Pattern pattern;
    private final Map<String, BaseCommand> registry = new HashMap<>();

    public CommandRegistry(JanitorBot bot, String prefix) {
        super(bot);
        this.pattern = Pattern.compile("^" + prefix + "([A-Za-z0-9]+).*$");

        addCommand("ping", new PingCommand(this));
        addCommand("ok", new OKCommand(this));
        if (bot.getConfig().getOwnerID().isPresent()) {
            addCommand("shutdown", new ShutdownCommand(this, bot.getConfig().getOwnerID().get()));
        }
    }

    public JanitorBot getBot() {
        return this.bot;
    }

    public void addCommand(String cmd, BaseCommand instance) {
        registry.put(cmd, instance);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            String msg = event.getMessage().getContentDisplay();
            Matcher matcher = pattern.matcher(msg);
            if (matcher.matches()) {
                String cmd = matcher.group(1);
                if (registry.containsKey(cmd)) {
                    JANITOR.debug(COMMANDS, "Received command: {}; author: {}, full message: {}", cmd,
                        Util.toString(event.getAuthor()), msg);
                    registry.get(cmd).onCommand(event);
                }
            }
        } catch (Exception e) {
            JANITOR.error(COMMANDS, "Error while parsing message: {}",
                event.getMessage().getContentStripped(), e);
        }
    }
}
