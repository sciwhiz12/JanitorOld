package sciwhiz12.janitor.storage;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.api.entities.Guild;
import sciwhiz12.janitor.JanitorBotImpl;
import sciwhiz12.janitor.api.Logging;
import sciwhiz12.janitor.api.storage.GuildStorageManager;
import sciwhiz12.janitor.api.storage.Storage;
import sciwhiz12.janitor.api.storage.StorageKey;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.nio.file.StandardOpenOption.*;

/**
 * A storage system for guild-specific data.
 */
public class GuildStorageManagerImpl implements GuildStorageManager {
    private final JanitorBotImpl bot;
    private final Path mainFolder;
    private final Map<Long, Map<String, InnerStorage<?>>> guildStorages = new HashMap<>();

    public GuildStorageManagerImpl(JanitorBotImpl bot, Path mainFolder) {
        Preconditions.checkArgument(Files.isDirectory(mainFolder) || Files.notExists(mainFolder));
        this.bot = bot;
        this.mainFolder = mainFolder;
    }

    @Override
    public JanitorBotImpl getBot() {
        return bot;
    }

    @Override
    public <S extends Storage> S getOrCreate(long guildID, StorageKey<S> key, Supplier<S> defaultSupplier) {
        final Map<String, InnerStorage<?>> storageMappy = guildStorages.computeIfAbsent(guildID, id -> new HashMap<>());
        return key.getType().cast(storageMappy.computeIfAbsent(key.getStorageID(),
            k -> new InnerStorage<>(key, load(guildID, key.getStorageID(), defaultSupplier.get()))).getStorage());
    }

    private Path getFile(long guildID, String key) {
        final Path guildFolder = Path.of(Long.toHexString(guildID));
        final Path file = Path.of(key + ".json");
        return mainFolder.resolve(guildFolder).resolve(file);
    }

    public <T extends Storage> T load(long guildID, String key, T storage) {
        final Path file = getFile(guildID, key);
        if (Files.notExists(file)) return storage;

        Logging.JANITOR.debug("Loading storage {} for guild {}", key, guildID);
        try (Reader reader = Files.newBufferedReader(file)) {
            storage.read(reader);
        } catch (IOException e) {
            Logging.JANITOR.error("Error while loading storage {} for guild {}", key, guildID, e);
        }
        return storage;
    }

    @Override
    public void save() {
        save(false);
    }

    public void save(boolean isAutosave) {
        if (!isAutosave)
            Logging.JANITOR.debug("Saving guild storage to files under {}...", mainFolder);
        boolean anySaved = false;
        for (long guildID : guildStorages.keySet()) {
            final Map<String, InnerStorage<?>> storageMap = guildStorages.get(guildID);
            for (String key : storageMap.keySet()) {
                final InnerStorage<?> inner = storageMap.get(key);
                if (inner.dirty()) {
                    final Path file = getFile(guildID, key);
                    try {
                        if (Files.notExists(file.getParent())) Files.createDirectories(file.getParent());
                        if (Files.notExists(file)) Files.createFile(file);
                        try (Writer writer = Files
                            .newBufferedWriter(file, CREATE, WRITE, TRUNCATE_EXISTING)) {
                            inner.getStorage().write(writer);
                            anySaved = true;
                        }
                    } catch (IOException e) {
                        Logging.JANITOR.error("Error while writing storage {} for guild {}", key, guildID, e);
                    }
                }
            }
        }
        if (anySaved)
            Logging.JANITOR.info("Saved guild storage to files under {}", mainFolder);
    }

    /**
     * A thread that calls {@link GuildStorageManagerImpl#save(boolean)} between specified delays.
     */
    public static class SavingThread extends Thread {
        private final GuildStorageManagerImpl storage;
        private volatile boolean running = true;

        public SavingThread(GuildStorageManagerImpl storage) {
            this.storage = storage;
            this.setName("GuildStorage-Saving-Thread");
            this.setDaemon(true);
        }

        public void stopThread() {
            running = false;
            this.interrupt();
        }

        @Override
        public void run() {
            while (running) {
                storage.save(true);
                try {
                    Thread.sleep(storage.getBot().getBotConfig().AUTOSAVE_INTERVAL.get() * 1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * <strong>For internal use only.</strong>
     */
    static class InnerStorage<S extends Storage> {
        private final StorageKey<S> key;
        private final S storage;

        InnerStorage(StorageKey<S> key, S storage) {
            this.key = key;
            this.storage = storage;
        }

        public StorageKey<S> getKey() {
            return key;
        }

        public S getStorage() {
            return storage;
        }

        public boolean dirty() {
            return storage.dirty();
        }
    }
}
