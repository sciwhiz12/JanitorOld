package sciwhiz12.janitor.storage;

import java.io.Reader;
import java.io.Writer;

public interface IStorage {

    boolean dirty();

    void write(Writer output);

    void read(Reader input);
}
