package sciwhiz12.janitor.storage;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * A storage key, used to retrieve an instance of an {@link IStorage} from a {@link GuildStorage}.
 *
 * @param <S> the type of the {@link IStorage}
 */
public class StorageKey<S extends IStorage> {
    private final String storageID;
    private final Class<S> type;

    /**
     * Creates a {@link StorageKey} with the given storage ID and type.
     *
     * @param storageID the storage ID
     * @param type      the class type of the generic type
     *
     * @throws NullPointerException     if {@code storageID} or {@code type} is {@code null}
     * @throws IllegalArgumentException if {@code storageID} is empty or blank
     */
    public StorageKey(String storageID, Class<S> type) {
        Preconditions.checkNotNull(storageID, "Storage ID must not be null");
        Preconditions.checkArgument(!storageID.isBlank(), "Storage ID must not be empty or blank");
        Preconditions.checkNotNull(type, "Class type must not be null");
        this.storageID = storageID;
        this.type = type;
    }

    /**
     * Returns the storage ID, used by {@link GuildStorage} to uniquely identify this storage's data.
     *
     * <p>This is currently used by {@code GuildStorage} as the folder name of the storage.
     *
     * @return the storage ID
     */
    public String getStorageID() {
        return storageID;
    }

    /**
     * Returns the class of the {@link IStorage} subtype that this storage key represents, which
     * is also used in the key's generics.
     *
     * @return the class of the generic type
     */
    public Class<S> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageKey<?> that = (StorageKey<?>) o;
        return storageID.equals(that.storageID) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageID, type);
    }
}
