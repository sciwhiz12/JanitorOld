package tk.sciwhiz12.janitor.api.core.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface Storage {
    boolean dirty();

    void write(Writer output) throws IOException;

    void read(Reader input) throws IOException;
}
