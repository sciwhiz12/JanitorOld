package sciwhiz12.janitor.commands.moderation;

import com.google.common.collect.ImmutableMap;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.member;
import static sciwhiz12.janitor.commands.util.CommandHelper.argument;
import static sciwhiz12.janitor.commands.util.CommandHelper.literal;

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

    public int run(CommandContext<MessageReceivedEvent> ctx, boolean filterTarget, boolean filterModerator)
        throws CommandSyntaxException {
        realRun(ctx, filterTarget, filterModerator);
        return 1;
    }

    void realRun(CommandContext<MessageReceivedEvent> ctx, boolean filterTarget, boolean filterModerator)
        throws CommandSyntaxException {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().GENERAL.guildOnlyCommand(channel).queue();
            return;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        Predicate<Map.Entry<Integer, WarningEntry>> predicate = e -> true;

        if (filterTarget) {
            final List<Member> members = getMembers("target", ctx).fromGuild(performer.getGuild());
            if (members.size() < 1) return;
            final Member target = members.get(0);
            if (guild.getSelfMember().equals(target)) {
                messages().GENERAL.cannotActionSelf(channel).queue();
                return;
            }
            predicate = predicate.and(e -> e.getValue().getWarned().getIdLong() == target.getIdLong());
        }
        if (filterModerator) {
            final List<Member> members = getMembers("moderator", ctx).fromGuild(performer.getGuild());
            if (members.size() < 1) return;
            final Member mod = members.get(0);
            predicate = predicate.and(e -> e.getValue().getPerformer().getIdLong() == mod.getIdLong());
        }

        final OffsetDateTime dateTime = OffsetDateTime.now();

        if (!performer.hasPermission(WARN_PERMISSION))
            messages().MODERATION.ERRORS.performerInsufficientPermissions(channel, performer, WARN_PERMISSION).queue();
        else
            messages().MODERATION.warnList(channel, WarningStorage.get(getBot().getStorage(), guild)
                .getWarnings()
                .entrySet().stream()
                .filter(predicate)
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue))
            ).queue();
    }
}
