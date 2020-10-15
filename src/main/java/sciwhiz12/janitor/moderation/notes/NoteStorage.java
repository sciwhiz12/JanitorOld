package sciwhiz12.janitor.moderation.notes;

import com.electronwill.nightconfig.core.utils.ObservedMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.storage.GuildStorage;
import sciwhiz12.janitor.storage.JsonStorage;
import sciwhiz12.janitor.storage.StorageKey;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class NoteStorage extends JsonStorage {
    private static final Type NOTE_MAP_TYPE = new TypeToken<Map<Integer, NoteEntry>>() {}.getType();
    public static final StorageKey<NoteStorage> KEY = new StorageKey<>("notes", NoteStorage.class);

    public static NoteStorage get(GuildStorage storage, Guild guild) {
        return storage.getOrCreate(guild, KEY, () -> new NoteStorage(storage.getBot()));
    }

    private final Gson gson;
    private final JanitorBot bot;
    private int lastID = 1;
    private final Map<Integer, NoteEntry> notes = new ObservedMap<>(new HashMap<>(), this::markDirty);

    public NoteStorage(JanitorBot bot) {
        this.bot = bot;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(NoteEntry.class, new NoteEntry.Serializer(bot))
                .create();
    }

    public JanitorBot getBot() {
        return bot;
    }

    public int addNote(NoteEntry entry) {
        int id = lastID++;
        notes.put(id, entry);
        return id;
    }

    @Nullable
    public NoteEntry getNote(int noteID) {
        return notes.get(noteID);
    }

    public NoteEntry removeNote(int noteID) {
        return notes.remove(noteID);
    }

    public int getAmountOfNotes(User target) {
        return (int) notes.values().stream()
                .filter(entry -> entry.getTarget() == target)
                .count();
    }

    public Map<Integer, NoteEntry> getNotes() {
        return notes;
    }

    @Override
    public JsonElement save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("lastNoteID", lastID);
        obj.add("notes", gson.toJsonTree(notes));
        return obj;
    }

    @Override
    public void load(JsonElement in) {
        final JsonObject obj = in.getAsJsonObject();
        lastID = obj.get("lastNoteID").getAsInt();
        final Map<Integer, NoteEntry> loaded = gson.fromJson(obj.get("notes"), NOTE_MAP_TYPE);
        notes.clear();
        notes.putAll(loaded);
    }
}
