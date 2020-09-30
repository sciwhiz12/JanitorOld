package sciwhiz12.janitor.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.io.Writer;

public abstract class JsonStorage extends AbstractStorage {
    public static final Gson GSON = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create();

    public abstract JsonElement save();

    public abstract void load(JsonElement object);

    @Override
    public void write(Writer input) {
        GSON.toJson(save(), input);
    }

    @Override
    public void read(Reader input) {
        load(JsonParser.parseReader(input));
    }
}
