package sciwhiz12.janitor.moderation.warns;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.api.utils.MessageHelper;
import sciwhiz12.janitor.moderation.ModBaseCommand;
import sciwhiz12.janitor.moderation.ModerationHelper;
import sciwhiz12.janitor.moderation.ModerationModuleImpl;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;

import static sciwhiz12.janitor.api.utils.CommandHelper.argument;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;
import static sciwhiz12.janitor.moderation.ModerationConfigs.*;

public class UnwarnCommand extends ModBaseCommand {
    public static final EnumSet<Permission> WARN_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public UnwarnCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(module, registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("unwarn")
            .requires(ctx -> config(ctx).forGuild(ENABLE_WARNS))
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
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

            return;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        int caseID = IntegerArgumentType.getInteger(ctx, "caseId");

        if (!performer.hasPermission(WARN_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", WARN_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else {
            final WarningStorage storage = getWarns(guild);
            @Nullable
            final WarningEntry entry = storage.getWarning(caseID);
            Member temp;
            if (entry == null) {
                messages().getRegularMessage("moderation/error/unwarn/no_case_found")
                    .apply(MessageHelper.member("performer", performer))
                    .with("case_id", () -> String.valueOf(caseID))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

            } else if (entry.getWarned().getIdLong() == performer.getIdLong()
                && !config(guild).forGuild(ALLOW_REMOVE_SELF_WARNINGS)) {
                messages().getRegularMessage("moderation/error/unwarn/cannot_unwarn_self")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(ModerationHelper.warningEntry("warning_entry", caseID, entry))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

            } else if (config(guild).forGuild(WARNS_RESPECT_MOD_ROLES)
                && (temp = guild.getMember(entry.getPerformer())) != null && !performer.canInteract(temp)) {
                messages().getRegularMessage("moderation/error/unwarn/cannot_remove_higher_mod")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(ModerationHelper.warningEntry("warning_entry", caseID, entry))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

            } else {
                storage.removeWarning(caseID);
                messages().getRegularMessage("moderation/unwarn/info")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(ModerationHelper.warningEntry("warning_entry", caseID, entry))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

            }
        }
    }
}
