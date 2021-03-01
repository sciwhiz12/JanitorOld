package tk.sciwhiz12.janitor.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Logging {
    public static final Marker STATUS = MarkerFactory.getMarker("STATUS");
    public static final Marker COMMANDS = MarkerFactory.getMarker("COMMANDS");
    public static final Marker MESSAGES = MarkerFactory.getMarker("MESSAGES");
    public static final Marker STORAGE = MarkerFactory.getMarker("STORAGE");
    public static final Marker MODULE = MarkerFactory.getMarker("MODULE");

    public static final Logger JANITOR = LoggerFactory.getLogger("janitor");
    public static final Logger CONFIG = LoggerFactory.getLogger("janitor.config");
}
