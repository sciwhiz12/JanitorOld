package sciwhiz12.janitor.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import sciwhiz12.janitor.api.messages.substitution.ModifiableSubstitutions;
import sciwhiz12.janitor.moderation.notes.NoteEntry;
import sciwhiz12.janitor.moderation.warns.WarningEntry;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import static sciwhiz12.janitor.api.utils.MessageHelper.DATE_TIME_FORMAT;
import static sciwhiz12.janitor.api.utils.MessageHelper.user;

public class ModerationHelper {
    public static AuditableRestAction<Void> kickUser(Guild guild, Member performer, Member target, @Nullable String reason) {
        StringBuilder auditReason = new StringBuilder();
        auditReason.append("Kicked by ")
            .append(performer.getUser().getAsTag())
            .append(" on ")
            .append(Instant.now().atOffset(ZoneOffset.UTC).format(DATE_TIME_FORMAT));
        if (reason != null)
            auditReason.append(" for reason: ").append(reason);
        return guild.kick(target, auditReason.toString());
    }

    public static AuditableRestAction<Void> banUser(Guild guild, Member performer, Member target, int deleteDuration,
        @Nullable String reason) {
        StringBuilder auditReason = new StringBuilder();
        auditReason.append("Banned by ")
            .append(performer.getUser().getAsTag())
            .append(" on ")
            .append(Instant.now().atOffset(ZoneOffset.UTC).format(DATE_TIME_FORMAT));
        if (reason != null)
            auditReason.append(" for reason: ").append(reason);
        return guild.ban(target, deleteDuration, auditReason.toString());
    }

    public static AuditableRestAction<Void> unbanUser(Guild guild, User target) {
        return guild.unban(target);
    }

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> warningEntry(String head, int caseID, WarningEntry entry) {
        return builder -> builder
            .with(head + ".case_id", () -> String.valueOf(caseID))
            .apply(user(head + ".performer", entry.getPerformer()))
            .apply(user(head + ".target", entry.getWarned()))
            .with(head + ".date_time", () -> entry.getDateTime().format(DATE_TIME_FORMAT))
            .with(head + ".reason", entry::getReason);
    }

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> noteEntry(String head, int noteID, NoteEntry entry) {
        return builder -> builder
            .with(head + ".note_id", () -> String.valueOf(noteID))
            .apply(user(head + ".performer", entry.getPerformer()))
            .apply(user(head + ".target", entry.getTarget()))
            .with(head + ".date_time", () -> entry.getDateTime().format(DATE_TIME_FORMAT))
            .with(head + ".contents", entry::getContents);
    }
}
