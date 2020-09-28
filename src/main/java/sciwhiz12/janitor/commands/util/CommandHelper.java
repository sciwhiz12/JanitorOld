package sciwhiz12.janitor.commands.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.msg.General;

import java.util.EnumSet;

public class CommandHelper {
    public static LiteralArgumentBuilder<MessageReceivedEvent> literal(String command) {
        return LiteralArgumentBuilder.literal(command);
    }

    public static <Arg> RequiredArgumentBuilder<MessageReceivedEvent, Arg> argument(String command, ArgumentType<Arg> argument) {
        return RequiredArgumentBuilder.argument(command, argument);
    }

    public static boolean canInteract(TextChannel response, Member target) {
        if (!target.getGuild().getSelfMember().canInteract(target)) {
            General.cannotInteract(response, target).queue();
            return false;
        }
        return true;
    }

    public static boolean hasPermission(TextChannel response, Guild guild, EnumSet<Permission> permissions) {
        if (!guild.getSelfMember().hasPermission(permissions)) {
            General.insufficientPermissions(response, permissions).queue();
            return false;
        }
        return true;
    }
}
