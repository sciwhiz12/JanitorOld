package tk.sciwhiz12.janitor.api.core.config;

import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;

public interface BotConfig {
    @Nullable
    Path getMessagesFolder();

    Path getConfigsFolder();

    String getToken();

    String getCommandPrefix();

    Optional<Long> getOwnerID();
}
