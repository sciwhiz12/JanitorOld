package tk.sciwhiz12.janitor.api.moderation.notes;

import net.dv8tion.jda.api.entities.User;

import java.time.OffsetDateTime;
import java.util.Objects;

public class NoteEntry {
    private final User performer;
    private final User target;
    private final OffsetDateTime dateTime;
    private final String contents;

    public NoteEntry(User performer, User target, OffsetDateTime dateTime, String contents) {
        this.performer = performer;
        this.target = target;
        this.dateTime = dateTime;
        this.contents = contents;
    }

    public User getPerformer() {
        return performer;
    }

    public User getTarget() {
        return target;
    }

    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteEntry noteEntry = (NoteEntry) o;
        return getPerformer().equals(noteEntry.getPerformer()) &&
            getTarget().equals(noteEntry.getTarget()) &&
            getDateTime().equals(noteEntry.getDateTime()) &&
            getContents().equals(noteEntry.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPerformer(), getTarget(), getDateTime(), getContents());
    }

}
