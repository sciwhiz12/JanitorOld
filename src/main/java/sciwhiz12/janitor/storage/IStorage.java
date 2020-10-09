package sciwhiz12.janitor.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface IStorage {

    boolean dirty();

    void write(Writer output) throws IOException;

    void read(Reader input) throws IOException;
}
