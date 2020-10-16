package sciwhiz12.janitor.msg.substitution;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IHasCustomSubstitutions<T extends IHasCustomSubstitutions<?>> {
    T with(String argument, Supplier<String> value);

    T apply(Consumer<T> consumer);
}
