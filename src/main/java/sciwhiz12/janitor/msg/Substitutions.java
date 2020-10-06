package sciwhiz12.janitor.msg;

import org.apache.commons.collections4.TransformerUtils;
import org.apache.commons.collections4.map.DefaultedMap;
import sciwhiz12.janitor.JanitorBot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class Substitutions {
    public static final Pattern ARGUMENT_REGEX = Pattern.compile("\\$\\{(.+?)}", CASE_INSENSITIVE);
    public static final Pattern NULL_ARGUMENT_REGEX = Pattern.compile("nullcheck;(.+?);(.+)", CASE_INSENSITIVE);

    public static String substitute(String text, Map<String, Supplier<String>> arguments) {
        final Matcher matcher = ARGUMENT_REGEX.matcher(text);
        return matcher.replaceAll(matchResult -> {
            final Matcher nullMatcher = NULL_ARGUMENT_REGEX.matcher(matchResult.group(1));
            if (nullMatcher.matches()) {
                final String str = arguments.get(nullMatcher.group(1)).get();
                return str != null ?
                    quoteReplacement(str) :
                    quoteReplacement(arguments.getOrDefault(nullMatcher.group(2), () -> nullMatcher.group(2)).get());
            }
            return quoteReplacement(arguments.getOrDefault(matchResult.group(1), () -> matchResult.group(0)).get());
        });
    }

    private final JanitorBot bot;
    private final Map<String, Supplier<String>> defaultSubstitutions = new HashMap<>();

    public Substitutions(JanitorBot bot) {
        this.bot = bot;
    }

    public JanitorBot getBot() {
        return bot;
    }

    public String substitute(String text) {
        return Substitutions.substitute(text, defaultSubstitutions);
    }

    public String with(String text, Map<String, Supplier<String>> substitutions) {
        return Substitutions.substitute(
            text,
            DefaultedMap.defaultedMap(substitutions, TransformerUtils.mapTransformer(defaultSubstitutions))
        );
    }

    public Map<String, Supplier<String>> createDefaultedMap(Map<String, Supplier<String>> custom) {
        return DefaultedMap.defaultedMap(custom, TransformerUtils.mapTransformer(defaultSubstitutions));
    }
}
