package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.EnumSet;
import java.util.stream.Collectors;

public final class General {
    public static final int FAILURE_COLOR = 0xF73132;

    private General() {
    }

    public static RestAction<Message> guildOnlyCommand(TextChannel channel) {
        return channel.sendMessage(
            new EmbedBuilder()
                .setTitle("Guild only command!")
                .setDescription("The previous command can only be run in a guild channel.")
                .setColor(FAILURE_COLOR)
                .build()
        );
    }

    public static RestAction<Message> insufficientPermissions(TextChannel channel, EnumSet<Permission> permissions) {
        return channel.sendMessage(
            new EmbedBuilder()
                .setTitle("I have insufficient permissions!")
                .setDescription("I do not have sufficient permissions to carry out this action!\n" +
                    "Please contact your server admins if you believe this is in error.")
                .addField(new MessageEmbed.Field(
                    "Required permissions",
                    permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")),
                    false))
                .setColor(FAILURE_COLOR)
                .build()
        );
    }

    public static RestAction<Message> ambiguousMember(TextChannel channel) {
        return channel.sendMessage(
            new EmbedBuilder()
                .setTitle("Ambiguous member argument!")
                .setDescription("The name you have specified is too ambiguous (leads to more than 1 member)!\n" +
                    "Please narrow down the specified name until it can uniquely identify a member of this guild.")
                .setColor(FAILURE_COLOR)
                .build()
        );
    }

    public static RestAction<Message> cannotInteract(TextChannel channel, Member target) {
        return channel.sendMessage(
            new EmbedBuilder()
                .setTitle("Member is higher than me!")
                .setDescription("Cannot perform action on the given member, as they higher up in the hierarchy than me.")
                .addField("Target", nameFor(target.getUser()), true)
                .setColor(General.FAILURE_COLOR)
                .build()
        );
    }

    public static String nameFor(User user) {
        return user.getName().concat("#").concat(user.getDiscriminator());
    }
}
