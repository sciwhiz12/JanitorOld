package sciwhiz12.janitor.msg.substitution;

import javax.annotation.Nullable;

public interface ISubstitutor {
    @Nullable
    String substitute(@Nullable String text);
}
