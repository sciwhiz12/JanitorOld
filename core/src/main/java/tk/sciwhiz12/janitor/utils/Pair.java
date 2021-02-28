package tk.sciwhiz12.janitor.utils;

import com.google.common.base.Preconditions;

import java.util.Map;

public class Pair<L, R> implements Map.Entry<L, R> {
    private final L left;
    private final R right;

    public static <L, R> Pair<L, R> of(L left, R right) {
        Preconditions.checkNotNull(left, "Left value should not be null");
        Preconditions.checkNotNull(right, "Right value should not be null");
        return new Pair<>(left, right);
    }

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public L getKey() {
        return left;
    }

    @Override
    public R getValue() {
        return right;
    }

    @Override
    public R setValue(R value) {
        throw new UnsupportedOperationException();
    }
}
