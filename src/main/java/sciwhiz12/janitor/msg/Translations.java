package sciwhiz12.janitor.msg;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import sciwhiz12.janitor.JanitorBot;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.TRANSLATIONS;

public class Translations {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DEFAULT_TRANSLATIONS_RESOURCE = "english.json";
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private final JanitorBot bot;
    private final Path translationsFile;
    private final Map<String, String> translations = new HashMap<>();

    public Translations(JanitorBot bot, Path translationsFile) {
        this.bot = bot;
        this.translationsFile = translationsFile;
        loadTranslations();
    }

    void loadTranslations() {
        if (translationsFile == null) {
            JANITOR.info(TRANSLATIONS, "No translation file given, using default english translations");
            loadDefaultTranslations();
            return;
        }
        try {
            JANITOR.debug(TRANSLATIONS, "Loading translations from file {}", translationsFile);
            Map<String, String> trans = GSON.fromJson(Files.newBufferedReader(translationsFile), MAP_TYPE);
            translations.clear();
            translations.putAll(trans);
            JANITOR.info(TRANSLATIONS, "Loaded {} translations from file {}", translations.size(), translationsFile);
        }
        catch (Exception e) {
            JANITOR.error(TRANSLATIONS, "Error while loading translations from file {}", translationsFile, e);
            loadDefaultTranslations();
        }
    }

    void loadDefaultTranslations() {
        try {
            JANITOR.debug(TRANSLATIONS, "Loading default english translations");
            // noinspection UnstableApiUsage
            Map<String, String> trans = GSON.fromJson(
                new InputStreamReader(Resources.getResource(DEFAULT_TRANSLATIONS_RESOURCE).openStream()),
                MAP_TYPE);
            translations.clear();
            translations.putAll(trans);
            JANITOR.info(TRANSLATIONS, "Loaded {} default english translations", translations.size());
        }
        catch (Exception e) {
            JANITOR.error(TRANSLATIONS, "Error while loading default english translations", e);
        }
    }

    public Map<String, String> getTranslationMap() {
        return Collections.unmodifiableMap(translations);
    }

    public String translate(String key, Object... args) {
        return String.format(translations.getOrDefault(key, key), args);
    }
}
