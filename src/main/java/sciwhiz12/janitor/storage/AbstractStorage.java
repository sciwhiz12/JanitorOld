package sciwhiz12.janitor.storage;

public abstract class AbstractStorage implements IStorage {
    private boolean dirty;

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean dirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        return false;
    }

    public void markDirty() {
        this.dirty = true;
    }
}
