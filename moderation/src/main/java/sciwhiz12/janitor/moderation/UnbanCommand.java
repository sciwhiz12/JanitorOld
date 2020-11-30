package sciwhiz12.janitor.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.api.utils.MessageHelper;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static sciwhiz12.janitor.api.utils.CommandHelper.argument;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;

public class UnbanCommand extends ModBaseCommand {
    public static final EnumSet<Permission> UNBAN_PERMISSION = EnumSet.of(Permission.BAN_MEMBERS);

    public UnbanCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(module, registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("unban")
            .then(argument("user", StringArgumentType.string())
                .executes(this::namedRun)
            ).then(argument("userID", longArg())
                .executes(this::idRun)
            );
    }

    public int namedRun(CommandContext<MessageReceivedEvent> ctx) {
        realNamedRun(ctx);
        return 1;
    }

    void realNamedRun(CommandContext<MessageReceivedEvent> ctx) {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

            return;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());

        final String username = getString(ctx, "user").toLowerCase(Locale.ROOT);
        guild.retrieveBanList()
            .map(list -> list.stream().parallel()
                .filter(ban -> ban.getUser().getAsTag().replaceAll("\\s", "").toLowerCase(Locale.ROOT)
                    .startsWith(username))
                .collect(Collectors.toList()))
            .queue(bans -> {
                if (bans.size() > 1) {
                    messages().getRegularMessage("general/error/ambiguous_member")
                        .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                        .send(getBot(), channel)
                        .reference(ctx.getSource().getMessage()).queue();

                } else if (bans.size() == 1) {
                    tryUnban(ctx.getSource().getMessage(), performer, bans.get(0).getUser());
                }
            });
    }

    public int idRun(CommandContext<MessageReceivedEvent> ctx) {
        realIdRun(ctx);
        return 1;
    }

    void realIdRun(CommandContext<MessageReceivedEvent> ctx) {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

            return;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());

        final long id = getLong(ctx, "userID");
        guild.retrieveBanList()
            .map(list -> list.stream().parallel()
                .filter(ban -> ban.getUser().getIdLong() == id)
                .collect(Collectors.toList()))
            .queue(bans -> {
                if (bans.size() != 1) {
                    return;
                }
                tryUnban(ctx.getSource().getMessage(), performer, bans.get(0).getUser());
            });
    }

    void tryUnban(Message originalMsg, Member performer, User target) {
        final MessageChannel channel = originalMsg.getChannel();
        final Guild guild = performer.getGuild();
        if (!guild.getSelfMember().hasPermission(UNBAN_PERMISSION)) {
            messages().getRegularMessage("general/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", UNBAN_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(originalMsg)
                .queue();

        } else if (!performer.hasPermission(UNBAN_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", UNBAN_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(originalMsg).queue();

        } else {
            ModerationHelper.unbanUser(guild, target)
                .flatMap(v -> messages().getRegularMessage("moderation/unban/info")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.user("target", target))
                    .send(getBot(), channel)
                    .reference(originalMsg)
                )
                .queue();
        }
    }
}
