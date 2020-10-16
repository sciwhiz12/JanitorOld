package sciwhiz12.janitor.msg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import sciwhiz12.janitor.JanitorBot;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.TRANSLATIONS;

public class TranslationMap {
    public static final Pattern TRANSLATION_REGEX = Pattern.compile("<(.+?)>", CASE_INSENSITIVE);
    private static final String DEFAULT_TRANSLATIONS_RESOURCE = "english.json";
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    private final JanitorBot bot;
    private final Path translationsFile;
    private final Map<String, String> translations = new HashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public TranslationMap(JanitorBot bot, Path translationsFile) {
        this.bot = bot;
        this.translationsFile = translationsFile;
        loadTranslations();
    }

    public void loadTranslations() {
        if (translationsFile == null) {
            JANITOR.info(TRANSLATIONS, "No translation file given, using default english translations");
            loadDefaultTranslations();
            return;
        }
        try {
            JANITOR.debug(TRANSLATIONS, "Loading translations from file {}", translationsFile);
            Map<String, String> trans = jsonMapper.readValue(Files.newBufferedReader(translationsFile), MAP_TYPE);
            translations.clear();
            translations.putAll(trans);
            JANITOR.info(TRANSLATIONS, "Loaded {} translations from file {}", translations.size(), translationsFile);
        } catch (Exception e) {
            JANITOR.error(TRANSLATIONS, "Error while loading translations from file {}", translationsFile, e);
            loadDefaultTranslations();
        }
    }

    void loadDefaultTranslations() {
        try {
            JANITOR.debug(TRANSLATIONS, "Loading default english translations");
            // noinspection UnstableApiUsage
            Map<String, String> trans = jsonMapper.readValue(
                new InputStreamReader(Resources.getResource(DEFAULT_TRANSLATIONS_RESOURCE).openStream()),
                MAP_TYPE);
            translations.clear();
            translations.putAll(trans);
            JANITOR.info(TRANSLATIONS, "Loaded {} default english translations", translations.size());
        } catch (Exception e) {
            JANITOR.error(TRANSLATIONS, "Error while loading default english translations", e);
        }
    }

    public Map<String, String> getTranslationMap() {
        return Collections.unmodifiableMap(translations);
    }

    public String translate(String text) {
        final Matcher matcher = TRANSLATION_REGEX.matcher(text);
        return matcher.replaceAll(
            matchResult -> quoteReplacement(translations.getOrDefault(matchResult.group(1), matchResult.group(0))));
    }

    public JanitorBot getBot() {
        return bot;
    }
}
