package sciwhiz12.janitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Logging {
    public static final Marker STATUS = MarkerFactory.getMarker("STATUS");
    public static final Marker COMMANDS = MarkerFactory.getMarker("COMMANDS");
    public static final Marker TRANSLATIONS = MarkerFactory.getMarker("TRANSLATIONS");

    public static final Logger JANITOR = LoggerFactory.getLogger("janitor");
    public static final Logger CONSOLE = LoggerFactory.getLogger("janitor.console");
    public static final Logger CONFIG = LoggerFactory.getLogger("janitor.config");
}
