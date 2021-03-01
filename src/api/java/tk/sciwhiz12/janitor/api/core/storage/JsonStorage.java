package tk.sciwhiz12.janitor.api.core.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public abstract class JsonStorage extends AbstractStorage {
    protected final ObjectMapper jsonMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    protected JsonStorage() {
        initialize(jsonMapper);
    }

    protected void initialize(ObjectMapper mapper) {}

    public abstract JsonNode save(ObjectMapper mapper);

    public abstract void load(JsonNode object, ObjectMapper mapper) throws IOException;

    @Override
    public void write(Writer input) throws IOException {
        jsonMapper.writeTree(jsonMapper.createGenerator(input), save(jsonMapper));
    }

    @Override
    public void read(Reader input) throws IOException {
        load(jsonMapper.readTree(input), jsonMapper);
    }
}
