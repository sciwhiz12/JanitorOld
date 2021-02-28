package tk.sciwhiz12.janitor.api.module;

import tk.sciwhiz12.janitor.api.JanitorBot;

public interface Module {
    void activate();

    void shutdown();

    JanitorBot getBot();
}
