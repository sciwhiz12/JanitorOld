package tk.sciwhiz12.janitor.moderation.warns;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tk.sciwhiz12.janitor.api.command.CommandRegistry;
import tk.sciwhiz12.janitor.api.utils.MessageHelper;
import tk.sciwhiz12.janitor.moderation.ModBaseCommand;
import tk.sciwhiz12.janitor.moderation.ModerationHelper;
import tk.sciwhiz12.janitor.moderation.ModerationModuleImpl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static tk.sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.getMembers;
import static tk.sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.member;
import static tk.sciwhiz12.janitor.api.utils.CommandHelper.argument;
import static tk.sciwhiz12.janitor.api.utils.CommandHelper.literal;
import static tk.sciwhiz12.janitor.moderation.ModerationConfigs.ALLOW_WARN_OTHER_MODERATORS;
import static tk.sciwhiz12.janitor.moderation.ModerationConfigs.ENABLE_WARNS;

public class WarnCommand extends ModBaseCommand {
    public static final EnumSet<Permission> WARN_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public WarnCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(module, registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("warn")
            .requires(ctx -> config(ctx).forGuild(ENABLE_WARNS))
            .then(argument("member", member())
                .then(argument("reason", greedyString())
                    .executes(ctx -> this.run(ctx, getString(ctx, "reason")))
                )
            );
    }

    int run(CommandContext<MessageReceivedEvent> ctx, String reason) throws CommandSyntaxException {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

            return 1;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());

        final List<Member> members = getMembers("member", ctx).fromGuild(performer.getGuild());
        if (members.size() < 1) { return 1; }
        final Member target = members.get(0);

        final OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);
        if (guild.getSelfMember().equals(target)) {
            messages().getRegularMessage("general/error/cannot_action_self")
                .apply(MessageHelper.member("performer", performer))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else if (performer.equals(target)) {
            messages().getRegularMessage("general/error/cannot_action_performer")
                .apply(MessageHelper.member("performer", performer))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else if (!performer.hasPermission(WARN_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", WARN_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else if (!performer.canInteract(target)) {
            messages().getRegularMessage("moderation/error/cannot_interact")
                .apply(MessageHelper.member("performer", performer))
                .apply(MessageHelper.member("target", target))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else if (target.hasPermission(WARN_PERMISSION) && config(guild).forGuild(ALLOW_WARN_OTHER_MODERATORS)) {
            messages().getRegularMessage("moderation/error/warn/cannot_warn_mods")
                .apply(MessageHelper.member("performer", performer))
                .apply(MessageHelper.member("target", target))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else {
            WarningEntry entry = new WarningEntry(performer.getUser(), target.getUser(), dateTime, reason);
            int caseId = getWarns(guild).addWarning(entry);

            target.getUser().openPrivateChannel()
                .flatMap(dm -> messages().getRegularMessage("moderation/warn/dm")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(ModerationHelper.warningEntry("warning_entry", caseId, entry))
                    .send(getBot(), dm)
                )
                .mapToResult()
                .flatMap(res -> messages().getRegularMessage("moderation/warn/info")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(ModerationHelper.warningEntry("warning_entry", caseId, entry))
                    .with("private_message", () -> res.isSuccess() ? "\u2705" : "\u274C")
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage())
                )
                .queue();
        }
        return 1;
    }
}
