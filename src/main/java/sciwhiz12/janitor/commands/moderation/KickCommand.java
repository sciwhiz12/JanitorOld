package sciwhiz12.janitor.commands.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.commands.util.CommandHelper;
import sciwhiz12.janitor.commands.util.ModerationHelper;
import sciwhiz12.janitor.msg.General;
import sciwhiz12.janitor.msg.Moderation;

import java.util.EnumSet;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.member;

public class KickCommand extends BaseCommand {
    public static final EnumSet<Permission> KICK_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public KickCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return CommandHelper.literal("kick")
            .then(
                CommandHelper.argument("member", member())
                    .then(
                        CommandHelper.argument("reason", greedyString())
                            .executes(ctx -> this.runWithReason(ctx, getString(ctx, "reason")))
                    )
                    .executes(this::run)
            );
    }

    private int run(CommandContext<MessageReceivedEvent> ctx) throws CommandSyntaxException {
        return runWithReason(ctx, null);
    }

    private int runWithReason(CommandContext<MessageReceivedEvent> ctx, @Nullable String reason) throws CommandSyntaxException {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            General.guildOnlyCommand(channel).queue();
            return 1;
        }
        Member performer = ctx.getSource().getMember();
        if (performer == null) return 1;
        List<Member> members = getMembers("member", ctx).fromGuild(performer.getGuild());
        Member target = members.get(0);
        if (ModerationHelper.ensurePermissions(channel, performer, target, KICK_PERMISSION)) {
            target.getUser().openPrivateChannel()
                .flatMap(dm -> Moderation.kickedDM(dm, performer, target, reason))
                .flatMap(v -> ModerationHelper.kickUser(target.getGuild(), performer, target, reason))
                .flatMap(v -> Moderation.kickUser(ctx.getSource().getChannel(), performer, target, reason))
                .queue();
        }
        return 1;
    }
}
