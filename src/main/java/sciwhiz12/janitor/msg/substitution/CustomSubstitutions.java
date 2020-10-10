package sciwhiz12.janitor.msg.substitution;

import java.util.Map;
import java.util.function.Supplier;

public class CustomSubstitutions implements ISubstitutor {
    private final Map<String, Supplier<String>> map;

    public CustomSubstitutions(Map<String, Supplier<String>> map) {
        this.map = map;
    }

    @Override
    public String substitute(String text) {
        return SubstitutionMap.substitute(text, map);
    }

    public Map<String, Supplier<String>> getMap() {
        return map;
    }
}
