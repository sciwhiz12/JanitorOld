package sciwhiz12.janitor.listeners;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sciwhiz12.janitor.JanitorBot;

public abstract class BaseListener extends ListenerAdapter {
    protected final JanitorBot bot;

    public BaseListener(JanitorBot bot) {
        this.bot = bot;
    }
}
