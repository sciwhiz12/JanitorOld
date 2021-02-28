package tk.sciwhiz12.janitor.api.config;

import com.google.common.base.Joiner;

import java.util.Objects;
import java.util.function.Supplier;

public class ConfigNode<T> {
    private static final Joiner NEWLINE = Joiner.on('\n');

    private final String path;
    private final String comment;
    private final Supplier<T> defaultValue;

    public ConfigNode(String path, Supplier<T> defaultValue, String... comment) {
        Objects.requireNonNull(path, "Config node path must not be null");
        Objects.requireNonNull(defaultValue, "Default value supplier must not be null");
        Objects.requireNonNull(comment, "Config node comments must not be null");
        this.path = path;
        this.defaultValue = defaultValue;
        this.comment = NEWLINE.join(comment);
    }

    public String path() {
        return path;
    }

    public String comment() {
        return comment;
    }

    public Supplier<T> defaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigNode<?> that = (ConfigNode<?>) o;
        return path.equals(that.path) &&
            comment.equals(that.comment) &&
            defaultValue.equals(that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, comment, defaultValue);
    }
}
