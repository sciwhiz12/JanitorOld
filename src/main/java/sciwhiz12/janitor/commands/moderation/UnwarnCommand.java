package sciwhiz12.janitor.commands.moderation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.moderation.warns.WarningEntry;
import sciwhiz12.janitor.moderation.warns.WarningStorage;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;

import static sciwhiz12.janitor.commands.util.CommandHelper.argument;
import static sciwhiz12.janitor.commands.util.CommandHelper.literal;

public class UnwarnCommand extends BaseCommand {
    public static final EnumSet<Permission> WARN_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public UnwarnCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("unwarn")
            .requires(ctx -> getBot().getConfig().WARNINGS_ENABLE.get())
            .then(argument("caseId", IntegerArgumentType.integer(1))
                .executes(this::run)
            );
    }

    public int run(CommandContext<MessageReceivedEvent> ctx) {
        realRun(ctx);
        return 1;
    }

    void realRun(CommandContext<MessageReceivedEvent> ctx) {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            channel.sendMessage(messages().GENERAL.guildOnlyCommand(ctx.getSource().getAuthor()).build(getBot())).queue();
            return;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        int caseID = IntegerArgumentType.getInteger(ctx, "caseId");

        if (!performer.hasPermission(WARN_PERMISSION))
            channel.sendMessage(
                messages().MODERATION.ERRORS.performerInsufficientPermissions(performer, WARN_PERMISSION).build(getBot()))
                .queue();
        else {
            final WarningStorage storage = WarningStorage.get(getBot().getStorage(), guild);
            @Nullable
            final WarningEntry entry = storage.getWarning(caseID);
            Member temp;
            if (entry == null)
                channel.sendMessage(messages().MODERATION.ERRORS.noWarnWithID(performer, caseID).build(getBot())).queue();
            else if (entry.getWarned().getIdLong() == performer.getIdLong()
                && !config().WARNINGS_REMOVE_SELF_WARNINGS.get())
                channel.sendMessage(messages().MODERATION.ERRORS.cannotUnwarnSelf(performer, caseID, entry).build(getBot()))
                    .queue();
            else if (config().WARNINGS_RESPECT_MOD_ROLES.get()
                && (temp = guild.getMember(entry.getPerformer())) != null
                && !performer.canInteract(temp))
                channel.sendMessage(
                    messages().MODERATION.ERRORS.cannotRemoveHigherModerated(performer, caseID, entry).build(getBot())).queue();
            else {
                storage.removeWarning(caseID);
                channel.sendMessage(messages().MODERATION.unwarn(performer, caseID, entry).build(getBot())).queue();
            }
        }
    }
}
