package sciwhiz12.janitor.api.module;

import sciwhiz12.janitor.api.JanitorBot;

public interface Module {
    void activate();

    void shutdown();

    JanitorBot getBot();
}
