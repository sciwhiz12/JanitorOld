package sciwhiz12.janitor.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.api.utils.CommandHelper;
import sciwhiz12.janitor.api.utils.MessageHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.member;

public class KickCommand extends ModBaseCommand {
    public static final EnumSet<Permission> KICK_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public KickCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(module, registry);
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

        } else if (!guild.getSelfMember().hasPermission(KICK_PERMISSION)) {
            messages().getRegularMessage("general/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", KICK_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else if (!guild.getSelfMember().canInteract(target)) {
            messages().getRegularMessage("general/error/cannot_interact")
                .apply(MessageHelper.member("target", target))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else if (!performer.hasPermission(KICK_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", KICK_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else if (!performer.canInteract(target)) {
            messages().getRegularMessage("moderation/error/cannot_interact")
                .apply(MessageHelper.member("performer", performer))
                .apply(MessageHelper.member("target", target))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else {
            target.getUser().openPrivateChannel()
                .flatMap(dm -> messages().getRegularMessage("moderation/kick/dm")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.member("target", target))
                    .with("reason", () -> reason)
                    .send(getBot(), dm)
                )
                .mapToResult()
                .flatMap(res -> ModerationHelper.kickUser(target.getGuild(), performer, target, reason)
                    .flatMap(v -> messages().getRegularMessage("moderation/kick/info")
                        .apply(MessageHelper.member("performer", performer))
                        .apply(MessageHelper.member("target", target))
                        .with("private_message", () -> res.isSuccess() ? "\u2705" : "\u274C")
                        .with("reason", () -> reason)
                        .send(getBot(), channel)
                        .reference(ctx.getSource().getMessage())
                    )
                )
                .queue();
        }
        return 1;
    }
}
