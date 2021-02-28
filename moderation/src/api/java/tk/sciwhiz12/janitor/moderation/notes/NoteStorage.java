package tk.sciwhiz12.janitor.moderation.notes;

import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.Nullable;
import tk.sciwhiz12.janitor.api.JanitorBot;
import tk.sciwhiz12.janitor.api.storage.Storage;
import tk.sciwhiz12.janitor.api.storage.StorageKey;

import java.util.Map;

public interface NoteStorage extends Storage {
    StorageKey<NoteStorage> KEY = new StorageKey<>("notes", NoteStorage.class);

    int addNote(NoteEntry entry);

    @Nullable NoteEntry getNote(int noteID);

    void removeNote(int noteID);

    int getAmountOfNotes(User target);

    Map<Integer, NoteEntry> getNotes();

    JanitorBot getBot();
}
