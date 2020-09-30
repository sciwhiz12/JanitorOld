package sciwhiz12.janitor.commands.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.moderation.warns.WarningEntry;
import sciwhiz12.janitor.moderation.warns.WarningStorage;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.member;
import static sciwhiz12.janitor.commands.util.CommandHelper.argument;
import static sciwhiz12.janitor.commands.util.CommandHelper.literal;

public class WarnCommand extends BaseCommand {
    public static final EnumSet<Permission> WARN_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public WarnCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("warn")
            .requires(ctx -> getBot().getConfig().WARNINGS_ENABLE.get())
            .then(argument("member", member())
                .then(argument("reason", greedyString())
                    .executes(ctx -> this.run(ctx, getString(ctx, "reason")))
                )
            );
    }

    public int run(CommandContext<MessageReceivedEvent> ctx, String reason) throws CommandSyntaxException {
        realRun(ctx, reason);
        return 1;
    }

    void realRun(CommandContext<MessageReceivedEvent> ctx, String reason) throws CommandSyntaxException {
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

        final OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);
        if (guild.getSelfMember().equals(target))
            messages().GENERAL.cannotActionSelf(channel).queue();
        else if (performer.equals(target))
            messages().GENERAL.cannotActionPerformer(channel, performer).queue();
        else if (!performer.hasPermission(WARN_PERMISSION))
            messages().MODERATION.performerInsufficientPermissions(channel, performer, WARN_PERMISSION).queue();
        else if (!performer.canInteract(target))
            messages().MODERATION.cannotModerate(channel, performer, target).queue();
        else if (target.hasPermission(WARN_PERMISSION) && config().WARNINGS_PREVENT_WARNING_MODS.get())
            messages().MODERATION.cannotWarnMods(channel, performer, target).queue();
        else
            target.getUser().openPrivateChannel()
                .flatMap(dm -> messages().MODERATION.warnDM(dm, performer, target, reason, dateTime))
                .mapToResult()
                .flatMap(res -> {
                    int caseId = WarningStorage.get(getBot().getStorage(), guild)
                        .addWarning(new WarningEntry(target.getUser(), performer.getUser(), dateTime, reason));
                    return messages().MODERATION
                        .warnUser(channel, performer, target, reason, dateTime, caseId, res.isSuccess());
                })
                .queue();
    }
}
