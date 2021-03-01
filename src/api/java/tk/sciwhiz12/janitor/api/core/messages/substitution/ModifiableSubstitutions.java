package tk.sciwhiz12.janitor.api.core.messages.substitution;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ModifiableSubstitutions<T extends ModifiableSubstitutions<?>> {
    T with(String argument, Supplier<String> value);

    T apply(Consumer<T> consumer);
}
