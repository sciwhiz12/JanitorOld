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
import sciwhiz12.janitor.api.utils.MessageHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.member;
import static sciwhiz12.janitor.api.utils.CommandHelper.argument;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;

public class BanCommand extends ModBaseCommand {
    public static final EnumSet<Permission> BAN_PERMISSION = EnumSet.of(Permission.BAN_MEMBERS);

    public BanCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(module, registry);
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

    int run(CommandContext<MessageReceivedEvent> ctx, int days, @Nullable String reason) throws CommandSyntaxException {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel).queue();

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
                .send(getBot(), channel).queue();

        } else if (performer.equals(target)) {
            messages().getRegularMessage("general/error/cannot_action_performer")
                .apply(MessageHelper.member("performer", performer))
                .send(getBot(), channel).queue();

        } else if (!guild.getSelfMember().hasPermission(BAN_PERMISSION)) {
            messages().getRegularMessage("general/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", BAN_PERMISSION::toString)
                .send(getBot(), channel).queue();

        } else if (!guild.getSelfMember().canInteract(target)) {
            messages().getRegularMessage("general/error/cannot_interact")
                .apply(MessageHelper.member("target", target))
                .send(getBot(), channel).queue();

        } else if (!performer.hasPermission(BAN_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", BAN_PERMISSION::toString)
                .send(getBot(), channel).queue();

        } else if (!performer.canInteract(target)) {
            messages().getRegularMessage("moderation/error/cannot_interact")
                .apply(MessageHelper.member("performer", performer))
                .apply(MessageHelper.member("target", target))
                .send(getBot(), channel).queue();

        } else {
            target.getUser().openPrivateChannel()
                .flatMap(dm -> messages().getRegularMessage("moderation/ban/dm")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.member("target", target))
                    .with("reason", () -> reason)
                    .send(getBot(), dm)
                )
                .mapToResult()
                .flatMap(res ->
                    ModerationHelper.banUser(target.getGuild(), performer, target, days, reason)
                        .flatMap(v -> messages().getRegularMessage("moderation/ban/info")
                            .apply(MessageHelper.member("performer", performer))
                            .apply(MessageHelper.member("target", target))
                            .with("private_message", () -> res.isSuccess() ? "\u2705" : "\u274C")
                            .with("delete_duration", () -> String.valueOf(days))
                            .with("reason", () -> reason)
                            .send(getBot(), channel)
                        )
                )
                .queue();
        }
        return 1;
    }
}
