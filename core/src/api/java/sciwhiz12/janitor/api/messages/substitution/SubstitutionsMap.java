package sciwhiz12.janitor.api.messages.substitution;

import sciwhiz12.janitor.api.JanitorBot;

import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public interface SubstitutionsMap extends Substitutor {
    Pattern ARGUMENT_REGEX = Pattern.compile("\\$\\{(.+?)}", CASE_INSENSITIVE);
    Pattern NULL_ARGUMENT_REGEX = Pattern.compile("nullcheck;(.+?);(.+)", CASE_INSENSITIVE);

    static String quote(@Nullable String input) {
        return input != null ? Matcher.quoteReplacement(input) : "";
    }

    @Nullable
    static String substitute(@Nullable String text, Map<String, Supplier<String>> arguments) {
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

    String with(String text, Map<String, Supplier<String>> substitutions);

    ModifiableSubstitutor<?> with(Map<String, Supplier<String>> customSubstitutions);

    Map<String, Supplier<String>> createDefaultedMap(Map<String, Supplier<String>> custom);

    JanitorBot getBot();
}
