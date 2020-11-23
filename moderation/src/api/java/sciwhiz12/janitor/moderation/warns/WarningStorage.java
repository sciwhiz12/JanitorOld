package sciwhiz12.janitor.moderation.warns;

import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.api.JanitorBot;
import sciwhiz12.janitor.api.storage.Storage;
import sciwhiz12.janitor.api.storage.StorageKey;

import java.util.Map;

public interface WarningStorage extends Storage {
    StorageKey<WarningStorage> KEY = new StorageKey<>("warnings", WarningStorage.class);

    JanitorBot getBot();

    int addWarning(WarningEntry entry);

    @Nullable WarningEntry getWarning(int caseID);

    void removeWarning(int caseID);

    Map<Integer, WarningEntry> getWarnings();
}
