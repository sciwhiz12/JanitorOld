package sciwhiz12.janitor.commands.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.commands.util.CommandHelper;
import sciwhiz12.janitor.commands.util.ModerationHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

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
            getBot().getMessages().GENERAL.guildOnlyCommand(channel).queue();
            return 1;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        final List<Member> members = getMembers("member", ctx).fromGuild(performer.getGuild());
        if (members.size() < 1) {
            return 1;
        }
        final Member target = members.get(0);
        if (!guild.getSelfMember().hasPermission(KICK_PERMISSION)) {
            getBot().getMessages().GENERAL.insufficientPermissions(channel, KICK_PERMISSION).queue();
            return 1;
        }
        if (!guild.getSelfMember().canInteract(target)) {
            getBot().getMessages().GENERAL.cannotInteract(channel, target).queue();
            return 1;
        }
        if (!performer.hasPermission(KICK_PERMISSION)) {
            getBot().getMessages().MODERATION.performerInsufficientPermissions(channel, performer, KICK_PERMISSION).queue();
            return 1;
        }
        if (!performer.canInteract(target)) {
            getBot().getMessages().MODERATION.cannotModerate(channel, performer, target).queue();
            return 1;
        }
        target.getUser().openPrivateChannel()
            .flatMap(dm -> getBot().getMessages().MODERATION.kickedDM(dm, performer, target, reason))
            .mapToResult()
            .flatMap(res -> ModerationHelper.kickUser(target.getGuild(), performer, target, reason)
                .flatMap(v -> getBot().getMessages().MODERATION.kickUser(channel, performer, target, reason, res.isSuccess())))
            .queue();
        return 1;
    }
}
