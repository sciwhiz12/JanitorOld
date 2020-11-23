package sciwhiz12.janitor.api.module;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class ModuleKey<M extends Module> {
    private final String moduleID;
    private final Class<M> type;

    public ModuleKey(String storageID, Class<M> type) {
        Preconditions.checkNotNull(storageID, "Module ID must not be null");
        Preconditions.checkArgument(!storageID.isBlank(), "Module ID must not be empty or blank");
        Preconditions.checkNotNull(type, "Class type must not be null");
        this.moduleID = storageID;
        this.type = type;
    }

    public String getModuleID() {
        return moduleID;
    }

    public Class<M> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleKey<?> that = (ModuleKey<?>) o;
        return moduleID.equals(that.moduleID) &&
            type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleID, type);
    }
}
