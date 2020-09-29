package sciwhiz12.janitor.commands.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.msg.General;
import sciwhiz12.janitor.msg.Moderation;

import java.util.EnumSet;

public class ModerationHelper {
    public static AuditableRestAction<Void> kickUser(Guild guild, Member performer, Member target, @Nullable String reason) {
        StringBuilder auditReason = new StringBuilder();
        auditReason.append("Kicked by ").append(General.nameFor(performer.getUser()));
        if (reason != null)
            auditReason.append(" for reason: ").append(reason);
        return guild.kick(target, auditReason.toString());
    }

    public static boolean ensurePermissions(MessageChannel channel, Member performer, Member target, EnumSet<Permission> permissions) {
        if (!CommandHelper.hasPermission(channel, target.getGuild(), permissions)) return false;
        if (!CommandHelper.canInteract(channel, target)) return false;
        if (!performer.hasPermission(permissions)) {
            Moderation.performerInsufficientPermissions(channel, performer, permissions).queue();
            return false;
        }
        if (!performer.canInteract(target)) {
            Moderation.cannotModerate(channel, performer, target).queue();
            return false;
        }
        return true;
    }
}
