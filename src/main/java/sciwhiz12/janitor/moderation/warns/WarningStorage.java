package sciwhiz12.janitor.moderation.warns;

import com.electronwill.nightconfig.core.utils.ObservedMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Guild;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.GuildStorage;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.storage.JsonStorage;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class WarningStorage extends JsonStorage {
    private static final Type WARNING_MAP_TYPE = new TypeToken<Map<Integer, WarningEntry>>() {}.getType();
    public static final String STORAGE_KEY = "warnings";

    public static WarningStorage get(GuildStorage storage, Guild guild) {
        return storage.getOrCreate(guild, STORAGE_KEY, () -> new WarningStorage(storage.getBot()));
    }

    private final Gson gson;
    private final JanitorBot bot;
    private int lastID = 1;
    private final Map<Integer, WarningEntry> warnings = new ObservedMap<>(new HashMap<>(), this::markDirty);

    public WarningStorage(JanitorBot bot) {
        this.bot = bot;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(WarningEntry.class, new WarningEntry.Serializer(bot))
            .create();
    }

    public JanitorBot getBot() {
        return bot;
    }

    public int addWarning(WarningEntry entry) {
        int id = lastID++;
        warnings.put(id, entry);
        return id;
    }

    @Nullable
    public WarningEntry getWarning(int caseID) {
        return warnings.get(caseID);
    }

    public WarningEntry removeWarning(int caseID) {
        return warnings.remove(caseID);
    }

    public Map<Integer, WarningEntry> getWarnings() {
        return warnings;
    }

    @Override
    public JsonElement save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("lastCaseID", lastID);
        obj.add("warnings", gson.toJsonTree(warnings));
        return obj;
    }

    @Override
    public void load(JsonElement in) {
        final JsonObject obj = in.getAsJsonObject();
        lastID = obj.get("lastCaseID").getAsInt();
        final Map<Integer, WarningEntry> loaded = gson.fromJson(obj.get("warnings"), WARNING_MAP_TYPE);
        warnings.clear();
        warnings.putAll(loaded);
    }
}
