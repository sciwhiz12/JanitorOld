package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.EnumSet;
import java.util.stream.Collectors;

public final class General {
    private final Messages messages;

    General(Messages messages) {
        this.messages = messages;
    }

    public MessageBuilder guildOnlyCommand(final User performer) {
        return messages.failure()
            .apply(builder -> messages.user(builder, "performer", performer))
            .embed(embed -> embed
                .setTitle("general.guild_only_command.title")
                .setDescription("general.guild_only_command.description")
            );
    }

    public MessageBuilder ambiguousMember(final Member performer) {
        return messages.failure()
            .apply(builder -> messages.member(builder, "performer", performer))
            .embed(embed -> embed
                .setTitle("general.ambiguous_member.title")
                .setDescription("general.ambiguous_member.description")
            );
    }

    public MessageBuilder insufficientPermissions(final Member performer, final EnumSet<Permission> permissions) {
        return messages.failure()
            .apply(builder -> messages.member(builder, "performer", performer))
            .with("required_permissions", () -> permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")))
            .embed(embed -> embed
                .setTitle("general.insufficient_permissions.title")
                .setDescription("general.insufficient_permissions.description")
            )
            .field("general.insufficient_permissions.field.permissions", true);
    }

    public MessageBuilder cannotInteract(final Member performer, final Member target) {
        return messages.failure()
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> messages.member(builder, "target", target))
            .embed(embed -> embed
                .setTitle("general.cannot_interact.title")
                .setDescription("general.cannot_interact.description")
            )
            .field("general.cannot_interact.field.target", true);
    }

    public MessageBuilder cannotActionSelf(final Member performer) {
        return messages.failure()
            .apply(builder -> messages.member(builder, "performer", performer))
            .embed(embed -> embed
                .setTitle("general.cannot_action_self.title")
                .setDescription("general.cannot_action_self.description")
            );
    }

    public MessageBuilder cannotActionPerformer(final Member performer) {
        return messages.failure()
            .apply(builder -> messages.member(builder, "performer", performer))
            .embed(embed -> embed
                .setTitle("general.cannot_action_performer.title")
                .setDescription("general.cannot_action_performer.description")
            )
            .field("general.cannot_action_performer.field.performer", true);
    }
}
