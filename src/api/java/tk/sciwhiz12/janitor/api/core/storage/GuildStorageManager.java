package tk.sciwhiz12.janitor.api.core.storage;

import net.dv8tion.jda.api.entities.Guild;
import tk.sciwhiz12.janitor.api.JanitorBot;

import java.util.function.Supplier;

public interface GuildStorageManager {
    default <S extends Storage> S getOrCreate(Guild guild, StorageKey<S> key, Supplier<S> defaultSupplier) {
        return getOrCreate(guild.getIdLong(), key, defaultSupplier);
    }

    <S extends Storage> S getOrCreate(long guildID, StorageKey<S> key, Supplier<S> defaultSupplier);

    void save();

    JanitorBot getBot();
}
