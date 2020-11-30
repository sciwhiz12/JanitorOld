package sciwhiz12.janitor.moderation.warns;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.moderation.ModBaseCommand;
import sciwhiz12.janitor.moderation.ModerationModuleImpl;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.member;
import static sciwhiz12.janitor.api.utils.CommandHelper.argument;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;
import static sciwhiz12.janitor.api.utils.MessageHelper.member;
import static sciwhiz12.janitor.api.utils.MessageHelper.user;
import static sciwhiz12.janitor.moderation.ModerationConfigs.ENABLE_WARNS;
import static sciwhiz12.janitor.moderation.ModerationHelper.warningEntry;

public class WarnListCommand extends ModBaseCommand {
    public static final EnumSet<Permission> WARN_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public WarnListCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(module, registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("warnlist")
            .requires(ctx -> config(ctx).forGuild(ENABLE_WARNS))
            .then(literal("target")
                .then(argument("target", member())
                    .then(literal("mod")
                        .then(argument("moderator", member())
                            .executes(ctx -> this.run(ctx, true, true))
                        )
                    )
                    .executes(ctx -> this.run(ctx, true, false))
                )
            ).then(literal("mod")
                .then(argument("moderator", member())
                    .executes(ctx -> this.run(ctx, false, true))
                )
            )
            .executes(ctx -> this.run(ctx, false, false));
    }

    int run(CommandContext<MessageReceivedEvent> ctx, boolean filterTarget, boolean filterModerator)
        throws CommandSyntaxException {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel).queue();

            return 1;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        Predicate<Map.Entry<Integer, WarningEntry>> predicate = e -> true;

        if (filterTarget) {
            final List<Member> members = getMembers("target", ctx).fromGuild(performer.getGuild());
            if (members.size() < 1) return 1;
            final Member target = members.get(0);
            if (guild.getSelfMember().equals(target)) {
                messages().getRegularMessage("general/error/cannot_interact")
                    .apply(member("target", target))
                    .send(getBot(), channel).queue();

                return 1;
            }
            predicate = predicate.and(e -> e.getValue().getWarned().getIdLong() == target.getIdLong());
        }
        if (filterModerator) {
            final List<Member> members = getMembers("moderator", ctx).fromGuild(performer.getGuild());
            if (members.size() < 1) return 1;
            final Member mod = members.get(0);
            predicate = predicate.and(e -> e.getValue().getPerformer().getIdLong() == mod.getIdLong());
        }

        if (!performer.hasPermission(WARN_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(member("performer", performer))
                .with("required_permissions", WARN_PERMISSION::toString)
                .send(getBot(), channel).queue();

        } else {
            messages().<Map.Entry<Integer, WarningEntry>>getListingMessage("moderation/warn/list")
                .apply(member("performer", performer))
                .setEntryApplier((entry, subs) ->
                    subs.apply(warningEntry("warning_entry", entry.getKey(), entry.getValue()))
                )
                .build(channel, getBot(), ctx.getSource().getMessage(),
                    getWarns(guild)
                        .getWarnings()
                        .entrySet().stream()
                        .filter(predicate)
                        .sorted(Comparator.<Map.Entry<Integer, WarningEntry>>comparingInt(Map.Entry::getKey).reversed())
                        .collect(ImmutableList.toImmutableList())
                )
                .queue();
        }
        return 1;
    }
}
