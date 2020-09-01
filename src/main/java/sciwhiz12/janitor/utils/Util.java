package sciwhiz12.janitor.utils;

import net.dv8tion.jda.api.entities.User;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {
    public static <T> T make(Supplier<T> creator, Consumer<T> configurator) {
        T obj = creator.get();
        configurator.accept(obj);
        return obj;
    }

    public static String toString(final User user) {
        return String.format("<%s#%s:%s>", user.getName(), user.getDiscriminator(), user.getId());
    }

    public static <Success, Error> BiConsumer<Success, Error> handle(final Consumer<Success> success,
                                                                     final Consumer<Error> exceptionally) {
        return (suc, ex) -> {
            if (ex == null) {
                success.accept(suc);
            } else {
                exceptionally.accept(ex);
            }
        };
    }
}
