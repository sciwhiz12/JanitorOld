package tk.sciwhiz12.janitor.moderation.notes;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tk.sciwhiz12.janitor.api.core.command.CommandRegistry;
import tk.sciwhiz12.janitor.api.core.utils.MessageHelper;
import tk.sciwhiz12.janitor.api.moderation.notes.NoteEntry;
import tk.sciwhiz12.janitor.api.moderation.notes.NoteStorage;
import tk.sciwhiz12.janitor.moderation.ModBaseCommand;
import tk.sciwhiz12.janitor.api.moderation.ModerationHelper;
import tk.sciwhiz12.janitor.moderation.ModerationModuleImpl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static tk.sciwhiz12.janitor.api.core.command.arguments.GuildMemberArgument.getMembers;
import static tk.sciwhiz12.janitor.api.core.command.arguments.GuildMemberArgument.member;
import static tk.sciwhiz12.janitor.api.core.utils.CommandHelper.argument;
import static tk.sciwhiz12.janitor.api.core.utils.CommandHelper.literal;
import static tk.sciwhiz12.janitor.api.core.utils.MessageHelper.user;
import static tk.sciwhiz12.janitor.moderation.ModerationConfigs.ENABLE_NOTES;
import static tk.sciwhiz12.janitor.moderation.ModerationConfigs.MAX_NOTES_PER_MOD;
import static tk.sciwhiz12.janitor.moderation.notes.NoteCommand.ModeratorFilter.*;

public class NoteCommand extends ModBaseCommand {
    public static EnumSet<Permission> NOTE_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public NoteCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(module, registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("note")
            .requires(event -> config(event).forGuild(ENABLE_NOTES))
            .then(literal("add")
                .then(argument("target", member())
                    .then(argument("contents", greedyString())
                        .executes(ctx -> this.addNote(ctx, getString(ctx, "contents")))
                    )
                )
            )
            .then(literal("list")
                .then(literal("mod")
                    .then(argument("moderator", member())
                        .then(argument("target", member())
                            .executes(ctx -> this.listNotes(ctx, true, ARGUMENT))
                        )
                        .executes(ctx -> this.listNotes(ctx, false, ARGUMENT))
                    )
                )
                .then(literal("me")
                    .then(argument("target", member())
                        .executes(ctx -> this.listNotes(ctx, true, PERFORMER))
                    )
                )
                .then(argument("target", member())
                    .executes(ctx -> this.listNotes(ctx, true, NONE))
                )
                .executes(ctx -> this.listNotes(ctx, false, NONE))
            )
            .then(literal("remove")
                .then(argument("noteId", integer(1))
                    .executes(ctx -> this.removeNote(ctx, getInteger(ctx, "noteId")))
                )
            )
            .then(literal("me")
                .then(argument("target", member())
                    .executes(ctx -> this.listNotes(ctx, true, PERFORMER))
                )
                .executes(ctx -> this.listNotes(ctx, false, PERFORMER))
            )
            .then(argument("target", member())
                .executes(ctx -> this.listNotes(ctx, true, NONE))
                .then(argument("contents", greedyString())
                    .executes(ctx -> this.addNote(ctx, getString(ctx, "contents")))
                )
            );
    }

    private int addNote(CommandContext<MessageReceivedEvent> ctx, String noteContents) throws CommandSyntaxException {
        final MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

            return 1;
        }
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        final Guild guild = performer.getGuild();
        final List<Member> members = getMembers("target", ctx).fromGuild(guild);
        if (members.size() < 1) return 1;
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

        } else if (!performer.hasPermission(NOTE_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", NOTE_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else {
            final NoteStorage storage = getNotes(guild);

            final int maxAmount = config(ctx.getSource()).forGuild(MAX_NOTES_PER_MOD);
            if (storage.getAmountOfNotes(target.getUser()) >= maxAmount) {
                messages().getRegularMessage("moderation/error/insufficient_permissions")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.member("target", target))
                    .with("notes_amount", () -> String.valueOf(maxAmount))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

            } else {
                final NoteEntry entry = new NoteEntry(performer.getUser(), target.getUser(), dateTime, noteContents);
                int noteID = storage.addNote(entry);

                messages().getRegularMessage("moderation/note/add")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(ModerationHelper.noteEntry("note_entry", noteID, entry))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

            }
        }
        return 1;
    }

    enum ModeratorFilter {
        NONE, PERFORMER, ARGUMENT
    }

    private int listNotes(CommandContext<MessageReceivedEvent> ctx, boolean filterTarget, ModeratorFilter modFilter)
        throws CommandSyntaxException {
        final MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

            return 1;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        Predicate<Map.Entry<Integer, NoteEntry>> predicate = e -> true;

        if (filterTarget) {
            final List<Member> members = getMembers("target", ctx).fromGuild(performer.getGuild());
            if (members.size() < 1) return 1;
            final Member target = members.get(0);
            if (guild.getSelfMember().equals(target)) {
                messages().getRegularMessage("general/error/cannot_interact")
                    .apply(MessageHelper.member("target", target))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

                return 1;
            }
            predicate = predicate.and(e -> e.getValue().getTarget().getIdLong() == target.getIdLong());
        }
        switch (modFilter) {
            case ARGUMENT: {
                final List<Member> members = getMembers("moderator", ctx).fromGuild(performer.getGuild());
                if (members.size() < 1) return 1;
                final Member mod = members.get(0);
                predicate = predicate.and(e -> e.getValue().getPerformer().getIdLong() == mod.getIdLong());
            }
            case PERFORMER: {
                predicate = predicate.and(e -> e.getValue().getPerformer().getIdLong() == performer.getIdLong());
            }
            case NONE: {}
        }

        if (!performer.hasPermission(NOTE_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", NOTE_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else {
            messages().<Map.Entry<Integer, NoteEntry>>getListingMessage("moderation/note/list")
                .apply(MessageHelper.member("performer", performer))
                .setEntryApplier((entry, subs) -> subs
                    .apply(ModerationHelper.noteEntry("note_entry", entry.getKey(), entry.getValue()))
                )
                .build(channel, getBot(), ctx.getSource().getMessage(),
                    getNotes(guild)
                        .getNotes()
                        .entrySet().stream()
                        .filter(predicate)
                        .sorted(Comparator.<Map.Entry<Integer, NoteEntry>>comparingInt(Map.Entry::getKey).reversed())
                        .collect(ImmutableList.toImmutableList())
                )
                .queue();
        }
        return 1;
    }

    private int removeNote(CommandContext<MessageReceivedEvent> ctx, int noteID) {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

            return 1;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());

        if (!performer.hasPermission(NOTE_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", NOTE_PERMISSION::toString)
                .send(getBot(), channel)
                .reference(ctx.getSource().getMessage()).queue();

        } else {
            final NoteStorage storage = getNotes(guild);
            @Nullable
            final NoteEntry entry = storage.getNote(noteID);
            if (entry == null) {
                messages().getRegularMessage("moderation/note/add")
                    .apply(MessageHelper.member("performer", performer))
                    .with("note_id", () -> String.valueOf(noteID))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();

            } else {
                storage.removeNote(noteID);

                messages().getRegularMessage("moderation/note/remove")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(ModerationHelper.noteEntry("note_entry", noteID, entry))
                    .send(getBot(), channel)
                    .reference(ctx.getSource().getMessage()).queue();
            }
        }
        return 1;
    }
}
