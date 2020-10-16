package sciwhiz12.janitor.commands.moderation;

import com.google.common.collect.ImmutableList;
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
import sciwhiz12.janitor.msg.MessageHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.member;
import static sciwhiz12.janitor.commands.util.CommandHelper.argument;
import static sciwhiz12.janitor.commands.util.CommandHelper.literal;
import static sciwhiz12.janitor.msg.MessageHelper.DATE_TIME_FORMAT;
import static sciwhiz12.janitor.msg.MessageHelper.user;

public class WarnListCommand extends BaseCommand {
    public static final EnumSet<Permission> WARN_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public WarnListCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("warnlist")
            .requires(ctx -> getBot().getConfig().WARNINGS_ENABLE.get())
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
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
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
                    .apply(MessageHelper.member("target", target))
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
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", WARN_PERMISSION::toString)
                .send(getBot(), channel).queue();

        } else {
            messages().<Map.Entry<Integer, WarningEntry>>getListingMessage("moderation/warn/list")
                .apply(MessageHelper.member("performer", performer))
                .amountPerPage(8)
                .setEntryApplier((entry, subs) -> subs
                    .with("warning_entry.case_id", () -> String.valueOf(entry.getKey()))
                    .apply(user("warning_entry.performer", entry.getValue().getPerformer()))
                    .apply(user("warning_entry.warned", entry.getValue().getWarned()))
                    .with("warning_entry.date_time", () -> entry.getValue().getDateTime().format(DATE_TIME_FORMAT))
                    .with("warning_entry.reason", entry.getValue()::getReason)
                )
                .build(channel, getBot(), ctx.getSource().getMessage(),
                    WarningStorage.get(getBot().getStorage(), guild)
                        .getWarnings()
                        .entrySet().stream()
                        .filter(predicate)
                        .sorted(Comparator.<Map.Entry<Integer, WarningEntry>>comparingInt(Map.Entry::getKey).reversed())
                        .collect(ImmutableList.toImmutableList())
                );
        }
        return 1;
    }
}
