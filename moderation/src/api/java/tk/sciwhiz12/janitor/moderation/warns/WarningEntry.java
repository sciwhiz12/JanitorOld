package tk.sciwhiz12.janitor.moderation.warns;

import net.dv8tion.jda.api.entities.User;

import java.time.OffsetDateTime;
import java.util.Objects;
import javax.annotation.Nullable;

public class WarningEntry {
    private final User performer;
    private final User warned;
    private final OffsetDateTime dateTime;
    @Nullable
    private final String reason;

    public WarningEntry(User performer, User warned, OffsetDateTime dateTime, @Nullable String reason) {
        this.performer = performer;
        this.warned = warned;
        this.dateTime = dateTime;
        this.reason = reason;
    }

    public User getPerformer() {
        return performer;
    }

    public User getWarned() {
        return warned;
    }

    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarningEntry that = (WarningEntry) o;
        return getPerformer().equals(that.getPerformer()) &&
            getWarned().equals(that.getWarned()) &&
            getDateTime().equals(that.getDateTime()) &&
            Objects.equals(getReason(), that.getReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPerformer(), getWarned(), getDateTime(), getReason());
    }

}
