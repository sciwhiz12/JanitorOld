package tk.sciwhiz12.janitor.api.moderation.warns;

import org.checkerframework.checker.nullness.qual.Nullable;
import tk.sciwhiz12.janitor.api.JanitorBot;
import tk.sciwhiz12.janitor.api.core.storage.Storage;
import tk.sciwhiz12.janitor.api.core.storage.StorageKey;

import java.util.Map;

public interface WarningStorage extends Storage {
    StorageKey<WarningStorage> KEY = new StorageKey<>("warnings", WarningStorage.class);

    JanitorBot getBot();

    int addWarning(WarningEntry entry);

    @Nullable WarningEntry getWarning(int caseID);

    void removeWarning(int caseID);

    Map<Integer, WarningEntry> getWarnings();
}
