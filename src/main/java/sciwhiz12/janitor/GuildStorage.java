package sciwhiz12.janitor;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.api.entities.Guild;
import sciwhiz12.janitor.storage.IStorage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.nio.file.StandardOpenOption.*;

public class GuildStorage {
    private final JanitorBot bot;
    private final Path mainFolder;
    private final Map<Guild, Map<String, IStorage>> guildStorage = new IdentityHashMap<>();

    public GuildStorage(JanitorBot bot, Path mainFolder) {
        Preconditions.checkArgument(Files.isDirectory(mainFolder) || Files.notExists(mainFolder));
        this.bot = bot;
        this.mainFolder = mainFolder;
    }

    public JanitorBot getBot() {
        return bot;
    }

    @SuppressWarnings("unchecked")
    public <T extends IStorage> T getOrCreate(Guild guild, String key, Supplier<T> defaultSupplier) {
        final Map<String, IStorage> storageMap = guildStorage.computeIfAbsent(guild, g -> new HashMap<>());
        return (T) storageMap.computeIfAbsent(key, k -> load(guild, key, defaultSupplier.get()));
    }

    private Path getFile(Guild guild, String key) {
        final Path guildFolder = makeFolder(guild);
        final Path file = Path.of(key + ".json");
        return mainFolder.resolve(guildFolder).resolve(file);
    }

    public <T extends IStorage> T load(Guild guild, String key, T storage) {
        final Path file = getFile(guild, key);
        if (Files.notExists(file)) return storage;

        Logging.JANITOR.debug("Loading storage {} for guild {}", key, guild);
        try (Reader reader = Files.newBufferedReader(file)) {
            storage.read(reader);
        }
        catch (IOException e) {
            Logging.JANITOR.error("Error while loading storage {} for guild {}", key, guild, e);
        }
        return storage;
    }

    public void save() {
        save(false);
    }

    public void save(boolean isAutosave) {
        if (!isAutosave)
            Logging.JANITOR.debug("Saving guild storage to files under {}...", mainFolder);
        boolean anySaved = false;
        for (Guild guild : guildStorage.keySet()) {
            final Map<String, IStorage> storageMap = guildStorage.get(guild);
            for (String key : storageMap.keySet()) {
                final IStorage storage = storageMap.get(key);
                if (storage.dirty()) {
                    final Path file = getFile(guild, key);
                    try {
                        if (Files.notExists(file.getParent())) Files.createDirectories(file.getParent());
                        if (Files.notExists(file)) Files.createFile(file);
                        try (Writer writer = Files
                            .newBufferedWriter(file, CREATE, WRITE, TRUNCATE_EXISTING)) {
                            storage.write(writer);
                            anySaved = true;
                        }
                    }
                    catch (IOException e) {
                        Logging.JANITOR.error("Error while writing storage {} for guild {}", key, guild, e);
                    }
                }
            }
        }
        if (anySaved)
            Logging.JANITOR.info("Saved guild storage to files under {}", mainFolder);
    }

    private Path makeFolder(Guild guild) {
        return Path.of(Long.toHexString(guild.getIdLong()));
    }

    public static class SavingThread extends Thread {
        private final GuildStorage storage;
        private volatile boolean running = true;

        public SavingThread(GuildStorage storage) {
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
                try { Thread.sleep(storage.getBot().getConfig().AUTOSAVE_INTERVAL.get() * 1000); }
                catch (InterruptedException ignored) {}
            }
        }
    }
}
