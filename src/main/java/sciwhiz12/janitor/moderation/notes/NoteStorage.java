package sciwhiz12.janitor.moderation.notes;

import com.electronwill.nightconfig.core.utils.ObservedMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.storage.GuildStorage;
import sciwhiz12.janitor.storage.JsonStorage;
import sciwhiz12.janitor.storage.StorageKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NoteStorage extends JsonStorage {
    private static final TypeReference<Map<Integer, NoteEntry>> NOTE_MAP_TYPE = new TypeReference<>() {};
    public static final StorageKey<NoteStorage> KEY = new StorageKey<>("notes", NoteStorage.class);

    public static NoteStorage get(GuildStorage storage, Guild guild) {
        return storage.getOrCreate(guild, KEY, () -> new NoteStorage(storage.getBot()));
    }

    private final JanitorBot bot;
    private int lastID = 1;
    private final Map<Integer, NoteEntry> notes = new ObservedMap<>(new HashMap<>(), this::markDirty);

    public NoteStorage(JanitorBot bot) {
        this.bot = bot;
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

    public void removeNote(int noteID) {
        notes.remove(noteID);
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
    protected void initialize(ObjectMapper mapper) {
        super.initialize(mapper);
        mapper.registerModule(
            new SimpleModule()
                .addSerializer(NoteEntry.class, new NoteEntry.Serializer())
                .addDeserializer(NoteEntry.class, new NoteEntry.Deserializer(this::getBot))
        );
    }

    @Override
    public JsonNode save(ObjectMapper mapper) {
        final ObjectNode obj = mapper.createObjectNode();
        obj.put("lastNoteID", lastID);
        obj.set("notes", mapper.valueToTree(notes));
        return obj;
    }

    @Override
    public void load(JsonNode in, ObjectMapper mapper) throws IOException {
        lastID = in.get("lastNoteID").asInt();
        final Map<Integer, NoteEntry> loaded = mapper.readerFor(NOTE_MAP_TYPE).readValue(in.get("notes"));
        notes.clear();
        notes.putAll(loaded);
    }
}
