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
import sciwhiz12.janitor.commands.util.ModerationHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.member;
import static sciwhiz12.janitor.commands.util.CommandHelper.argument;
import static sciwhiz12.janitor.commands.util.CommandHelper.literal;

public class BanCommand extends BaseCommand {
    public static final EnumSet<Permission> BAN_PERMISSION = EnumSet.of(Permission.BAN_MEMBERS);

    /*
      ban command
     !ban <user> [reason]
     !ban delete <number of days> <user> [reason]
     */

    public BanCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("ban")
            .then(argument("member", member())
                .then(argument("reason", greedyString())
                    .executes(ctx -> this.run(ctx, 0, getString(ctx, "reason")))
                )
                .executes(ctx -> this.run(ctx, 0, null))
            )
            .then(literal("delete")
                .then(argument("days", integer(0, 7))
                    .then(argument("member", member())
                        .then(argument("reason", greedyString())
                            .executes(ctx -> this.run(ctx, getInteger(ctx, "days"), getString(ctx, "reason")))
                        )
                        .executes(ctx -> this.run(ctx, getInteger(ctx, "days"), null))
                    )
                )
            );
    }

    public int run(CommandContext<MessageReceivedEvent> ctx, int days, @Nullable String reason) throws CommandSyntaxException {
        realRun(ctx, days, reason);
        return 1;
    }

    void realRun(CommandContext<MessageReceivedEvent> ctx, int days, @Nullable String reason) throws CommandSyntaxException {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().GENERAL.guildOnlyCommand(channel).queue();
            return;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());

        final List<Member> members = getMembers("member", ctx).fromGuild(performer.getGuild());
        if (members.size() < 1) return;
        final Member target = members.get(0);

        if (guild.getSelfMember().equals(target))
            messages().GENERAL.cannotActionSelf(channel).queue();
        else if (performer.equals(target))
            messages().GENERAL.cannotActionPerformer(channel, performer).queue();
        else if (!guild.getSelfMember().hasPermission(BAN_PERMISSION))
            messages().GENERAL.insufficientPermissions(channel, BAN_PERMISSION).queue();
        else if (!guild.getSelfMember().canInteract(target))
            messages().GENERAL.cannotInteract(channel, target).queue();
        else if (!performer.hasPermission(BAN_PERMISSION))
            messages().MODERATION.ERRORS.performerInsufficientPermissions(channel, performer, BAN_PERMISSION).queue();
        else if (!performer.canInteract(target))
            messages().MODERATION.ERRORS.cannotModerate(channel, performer, target).queue();
        else
            target.getUser().openPrivateChannel()
                .flatMap(dm -> messages().MODERATION.bannedDM(dm, performer, reason))
                .mapToResult()
                .flatMap(res -> ModerationHelper.banUser(target.getGuild(), performer, target, days, reason)
                    .flatMap(
                        v -> messages().MODERATION.banUser(channel, performer, target, reason, days, res.isSuccess())))
                .queue();
    }
}
