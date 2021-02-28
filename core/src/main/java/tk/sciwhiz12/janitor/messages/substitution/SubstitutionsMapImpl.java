package tk.sciwhiz12.janitor.messages.substitution;

import org.apache.commons.collections4.TransformerUtils;
import org.apache.commons.collections4.map.DefaultedMap;
import tk.sciwhiz12.janitor.JanitorBotImpl;
import tk.sciwhiz12.janitor.api.messages.substitution.SubstitutionsMap;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static tk.sciwhiz12.janitor.api.utils.MessageHelper.DATE_TIME_FORMAT;

public class SubstitutionsMapImpl implements SubstitutionsMap {
    public static final Pattern ARGUMENT_REGEX = Pattern.compile("\\$\\{(.+?)}", CASE_INSENSITIVE);
    public static final Pattern NULL_ARGUMENT_REGEX = Pattern.compile("nullcheck;(.+?);(.+)", CASE_INSENSITIVE);

    private static String quote(@Nullable String input) {
        return input != null ? Matcher.quoteReplacement(input) : "";
    }

    @Nullable
    public static String substitute(@Nullable String text, Map<String, Supplier<String>> arguments) {
        if (text == null || text.isBlank()) return null;
        final Matcher matcher = ARGUMENT_REGEX.matcher(text);
        return matcher.replaceAll(matchResult -> {
            final Matcher nullMatcher = NULL_ARGUMENT_REGEX.matcher(matchResult.group(1));
            if (nullMatcher.matches()) {
                final String grp1 = nullMatcher.group(1);
                return quote(arguments.getOrDefault(
                    grp1,
                    () -> arguments.getOrDefault(nullMatcher.group(2), () -> nullMatcher.group(2)).get()
                ).get());
            }
            return quote(arguments.getOrDefault(matchResult.group(1), () -> matchResult.group(0)).get());
        });
    }

    private final JanitorBotImpl bot;
    private final Map<String, Supplier<String>> defaultSubstitutions = new HashMap<>();

    public SubstitutionsMapImpl(JanitorBotImpl bot) {
        this.bot = bot;
        defaultSubstitutions.put("time.now", () -> OffsetDateTime.now(ZoneOffset.UTC).format(DATE_TIME_FORMAT));
        defaultSubstitutions.put("moderation.color", () -> "0xF1BD25");
        defaultSubstitutions.put("moderation.icon_url",
            () -> "https://cdn.discordapp.com/attachments/738478941760782526/760463743330549760/gavel.png");
        defaultSubstitutions.put("general.error.color", () -> "0xF73132");
    }

    public JanitorBotImpl getBot() {
        return bot;
    }

    @Nullable
    public String substitute(@Nullable String text) {
        return SubstitutionsMap.substitute(text, defaultSubstitutions);
    }

    public String with(String text, Map<String, Supplier<String>> substitutions) {
        return SubstitutionsMap.substitute(text, createDefaultedMap(substitutions));
    }

    public CustomSubstitutions with(Map<String, Supplier<String>> customSubstitutions) {
        return new CustomSubstitutions(createDefaultedMap(customSubstitutions));
    }

    public Map<String, Supplier<String>> createDefaultedMap(Map<String, Supplier<String>> custom) {
        return DefaultedMap.defaultedMap(custom, TransformerUtils.mapTransformer(defaultSubstitutions));
    }
}
