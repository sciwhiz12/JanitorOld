package sciwhiz12.janitor.commands.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.checkerframework.checker.nullness.qual.Nullable;

import static sciwhiz12.janitor.utils.Util.nameFor;

public class ModerationHelper {
    public static AuditableRestAction<Void> kickUser(Guild guild, Member performer, Member target, @Nullable String reason) {
        StringBuilder auditReason = new StringBuilder();
        auditReason.append("Kicked by ").append(nameFor(performer.getUser()));
        if (reason != null)
            auditReason.append(" for reason: ").append(reason);
        return guild.kick(target, auditReason.toString());
    }
}
