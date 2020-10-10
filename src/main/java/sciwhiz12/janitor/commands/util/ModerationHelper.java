package sciwhiz12.janitor.commands.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import java.time.Instant;
import java.time.ZoneOffset;
import javax.annotation.Nullable;

import static sciwhiz12.janitor.msg.MessageHelper.DATE_TIME_FORMAT;
import static sciwhiz12.janitor.utils.Util.nameFor;

public class ModerationHelper {
    public static AuditableRestAction<Void> kickUser(Guild guild, Member performer, Member target, @Nullable String reason) {
        StringBuilder auditReason = new StringBuilder();
        auditReason.append("Kicked by ")
            .append(nameFor(performer.getUser()))
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
            .append(nameFor(performer.getUser()))
            .append(" on ")
            .append(Instant.now().atOffset(ZoneOffset.UTC).format(DATE_TIME_FORMAT));
        if (reason != null)
            auditReason.append(" for reason: ").append(reason);
        return guild.ban(target, deleteDuration, auditReason.toString());
    }

    public static AuditableRestAction<Void> unbanUser(Guild guild, User target) {
        return guild.unban(target);
    }
}
