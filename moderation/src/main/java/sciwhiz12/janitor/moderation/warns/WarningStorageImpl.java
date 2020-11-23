package sciwhiz12.janitor.moderation.warns;

import com.electronwill.nightconfig.core.utils.ObservedMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.api.JanitorBot;
import sciwhiz12.janitor.api.storage.JsonStorage;

import java.util.HashMap;
import java.util.Map;

public class WarningStorageImpl extends JsonStorage implements WarningStorage {
    private static final TypeReference<Map<Integer, WarningEntry>> WARNING_MAP_TYPE = new TypeReference<>() {};

    private final JanitorBot bot;
    private int lastID = 1;
    private final Map<Integer, WarningEntry> warnings = new ObservedMap<>(new HashMap<>(), this::markDirty);

    public WarningStorageImpl(JanitorBot bot) {
        this.bot = bot;
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

    public void removeWarning(int caseID) {
        warnings.remove(caseID);
    }

    public Map<Integer, WarningEntry> getWarnings() {
        return warnings;
    }

    @Override
    protected void initialize(ObjectMapper mapper) {
        super.initialize(mapper);
        mapper.registerModule(
            new SimpleModule()
                .addSerializer(WarningEntry.class, new WarningEntrySerializer())
                .addDeserializer(WarningEntry.class, new WarningEntryDeserializer(this::getBot))
        );
    }

    @Override
    public JsonNode save(ObjectMapper mapper) {
        final ObjectNode obj = mapper.createObjectNode();
        obj.put("lastCaseID", lastID);
        obj.set("warnings", mapper.valueToTree(warnings));
        return obj;
    }

    @Override
    public void load(JsonNode in, ObjectMapper mapper) {
        lastID = in.get("lastCaseID").asInt();
        final Map<Integer, WarningEntry> loaded = mapper.convertValue(in.get("warnings"), WARNING_MAP_TYPE);
        warnings.clear();
        warnings.putAll(loaded);
    }
}
