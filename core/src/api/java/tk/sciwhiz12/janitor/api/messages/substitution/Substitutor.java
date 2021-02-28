package tk.sciwhiz12.janitor.api.messages.substitution;

import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public interface Substitutor extends UnaryOperator<String> {
    @Override
    @Nullable
    default String apply(@Nullable String input) {
        return substitute(input);
    }

    @Nullable
    String substitute(@Nullable String text);
}
