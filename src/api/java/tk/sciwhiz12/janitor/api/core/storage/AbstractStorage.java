package tk.sciwhiz12.janitor.api.core.storage;

public abstract class AbstractStorage implements Storage {
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
