package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static sciwhiz12.janitor.msg.General.nameFor;

public final class Moderation {
    public static final int MODERATION_COLOR = 0xF1BD25;

    private Moderation() {
    }

    public static MessageAction performerInsufficientPermissions(MessageChannel channel, Member performer, EnumSet<Permission> permissions) {
        return channel.sendMessage(
            new EmbedBuilder()
                .setTitle("Insufficient permissions.")
                .setDescription("The performer of this command has insufficient permissions to use this command.")
                .addField("Performer", nameFor(performer.getUser()), true)
                .addField(new MessageEmbed.Field(
                    "Required permissions",
                    permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")),
                    true))
                .setColor(General.FAILURE_COLOR)
                .build()
        );
    }

    public static MessageAction cannotModerate(MessageChannel channel, Member performer, Member target) {
        return channel.sendMessage(
            new EmbedBuilder()
                .setTitle("Cannot moderate Target.")
                .setDescription("The performer of this command cannot moderate the target user, likely due to being lower in the role hierarchy.")
                .addField("Performer", nameFor(performer.getUser()), true)
                .addField("Target", nameFor(target.getUser()), true)
                .setColor(General.FAILURE_COLOR)
                .build()
        );
    }

    public static MessageAction kickUser(MessageChannel channel, Member performer, Member target, @Nullable String reason) {
        final EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Kicked.")
            .addField("Performer", nameFor(performer.getUser()), true)
            .addField("Target", nameFor(target.getUser()), true);
        if (reason != null)
            embed.addField("Reason", reason, false);
        return channel.sendMessage(
            embed.setColor(MODERATION_COLOR)
                .build()
        );
    }
}
