package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.EnumSet;
import java.util.stream.Collectors;

public final class General {
    private final Messages messages;

    General(Messages messages) {
        this.messages = messages;
    }

    private String translate(String key, Object... args) {
        return messages.translate(key, args);
    }

    public RestAction<Message> guildOnlyCommand(MessageChannel channel) {
        return channel.sendMessage(
            messages.failureEmbed(translate("general.guild_only_command.title"))
                .setDescription(translate("general.guild_only_command.desc"))
                .build()
        );
    }

    public RestAction<Message> insufficientPermissions(MessageChannel channel, EnumSet<Permission> permissions) {
        return channel.sendMessage(
            messages.failureEmbed(translate("general.insufficient_permissions.title"))
                .setDescription(translate("general.insufficient_permissions.desc"))
                .addField(new MessageEmbed.Field(
                    translate("general.insufficient_permissions.field.permissions"),
                    permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")),
                    false))
                .build()
        );
    }

    public RestAction<Message> ambiguousMember(MessageChannel channel) {
        return channel.sendMessage(
            messages.failureEmbed(translate("general.ambiguous_member.title"))
                .setDescription(translate("general.ambiguous_member.desc"))
                .build()
        );
    }

    public RestAction<Message> cannotInteract(MessageChannel channel, Member target) {
        return channel.sendMessage(
            messages.failureEmbed(translate("general.cannot_interact.title"))
                .setDescription(translate("general.cannot_interact.desc"))
                .addField(translate("general.cannot_interact.field.target"), target.getAsMention(), true)
                .build()
        );
    }

    public RestAction<Message> cannotActionSelf(MessageChannel channel) {
        return channel.sendMessage(
            messages.failureEmbed(translate("general.cannot_action_self.title"))
                .setDescription(translate("general.cannot_action_self.desc"))
                .build()
        );
    }

    public RestAction<Message> cannotActionPerformer(MessageChannel channel, Member performer) {
        return channel.sendMessage(
            messages.failureEmbed(translate("general.cannot_action_performer.title"))
                .setDescription(translate("general.cannot_action_performer.desc"))
                .addField(translate("general.cannot_action_performer.field.performer"),
                    performer.getUser().getAsMention(),
                    true)
                .build()
        );
    }
}
