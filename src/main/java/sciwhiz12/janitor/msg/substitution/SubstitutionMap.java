package sciwhiz12.janitor.msg.substitution;

import org.apache.commons.collections4.TransformerUtils;
import org.apache.commons.collections4.map.DefaultedMap;
import sciwhiz12.janitor.JanitorBot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static sciwhiz12.janitor.msg.MessageHelper.DATE_TIME_FORMAT;

public class SubstitutionMap implements ISubstitutor {
    public static final Pattern ARGUMENT_REGEX = Pattern.compile("\\$\\{(.+?)}", CASE_INSENSITIVE);
    public static final Pattern NULL_ARGUMENT_REGEX = Pattern.compile("nullcheck;(.+?);(.+)", CASE_INSENSITIVE);

    public static String substitute(String text, Map<String, Supplier<String>> arguments) {
        final Matcher matcher = ARGUMENT_REGEX.matcher(text);
        return matcher.replaceAll(matchResult -> {
            final Matcher nullMatcher = NULL_ARGUMENT_REGEX.matcher(matchResult.group(1));
            if (nullMatcher.matches()) {
                final String grp1 = nullMatcher.group(1);
                final String str = arguments.containsKey(grp1) ? arguments.get(grp1).get() : null;
                return str != null ?
                    quoteReplacement(str) :
                    quoteReplacement(arguments.getOrDefault(nullMatcher.group(2), () -> nullMatcher.group(2)).get());
            }
            return quoteReplacement(arguments.getOrDefault(matchResult.group(1), () -> matchResult.group(0)).get());
        });
    }

    private final JanitorBot bot;
    private final Map<String, Supplier<String>> defaultSubstitutions = new HashMap<>();

    public SubstitutionMap(JanitorBot bot) {
        this.bot = bot;
        defaultSubstitutions.put("time.now", () -> OffsetDateTime.now(ZoneOffset.UTC).format(DATE_TIME_FORMAT));
        defaultSubstitutions.put("moderation.color", () -> "0xF1BD25");
        defaultSubstitutions.put("moderation.icon_url",
            () -> "https://cdn.discordapp.com/attachments/738478941760782526/760463743330549760/gavel.png");
        defaultSubstitutions.put("general.error.color", () -> "0xF73132");
    }

    public JanitorBot getBot() {
        return bot;
    }

    public String substitute(String text) {
        return SubstitutionMap.substitute(text, defaultSubstitutions);
    }

    public String with(String text, Map<String, Supplier<String>> substitutions) {
        return SubstitutionMap.substitute(text, createDefaultedMap(substitutions));
    }

    public CustomSubstitutions with(Map<String, Supplier<String>> customSubstitutions) {
        return new CustomSubstitutions(createDefaultedMap(customSubstitutions));
    }

    public Map<String, Supplier<String>> createDefaultedMap(Map<String, Supplier<String>> custom) {
        return DefaultedMap.defaultedMap(custom, TransformerUtils.mapTransformer(defaultSubstitutions));
    }

}
