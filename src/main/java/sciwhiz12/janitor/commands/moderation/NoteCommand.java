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
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.commands.BaseCommand;
import sciwhiz12.janitor.commands.CommandRegistry;
import sciwhiz12.janitor.moderation.notes.NoteEntry;
import sciwhiz12.janitor.moderation.notes.NoteStorage;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.commands.arguments.GuildMemberArgument.member;
import static sciwhiz12.janitor.commands.moderation.NoteCommand.ModeratorFilter.*;
import static sciwhiz12.janitor.commands.util.CommandHelper.argument;
import static sciwhiz12.janitor.commands.util.CommandHelper.literal;

public class NoteCommand extends BaseCommand {
    public static EnumSet<Permission> NOTE_PERMISSION = EnumSet.of(Permission.KICK_MEMBERS);

    public NoteCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("note")
            .requires(ctx -> config().NOTES_ENABLE.get())
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
            channel.sendMessage(messages().GENERAL.guildOnlyCommand(ctx.getSource().getAuthor()).build(getBot())).queue();
            return 1;
        }
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());
        final Guild guild = performer.getGuild();
        final List<Member> members = getMembers("target", ctx).fromGuild(guild);
        if (members.size() < 1) return 1;
        final Member target = members.get(0);
        final OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);

        if (guild.getSelfMember().equals(target))
            channel.sendMessage(messages().GENERAL.cannotActionSelf(performer).build(getBot())).queue();
        else if (performer.equals(target))
            channel.sendMessage(messages().GENERAL.cannotActionPerformer(performer).build(getBot())).queue();
        else if (!performer.hasPermission(NOTE_PERMISSION))
            channel.sendMessage(
                messages().MODERATION.ERRORS.performerInsufficientPermissions(performer, NOTE_PERMISSION).build(getBot()))
                .queue();
        else {
            final NoteStorage storage = NoteStorage.get(getBot().getStorage(), guild);
            final int maxAmount = config().NOTES_MAX_AMOUNT_PER_MOD.get();
            if (storage.getAmountOfNotes(target.getUser()) >= maxAmount) {
                channel.sendMessage(messages().MODERATION.ERRORS.maxAmountOfNotes(performer, target, maxAmount).build(getBot()))
                    .queue();
            } else {
                final NoteEntry entry = new NoteEntry(performer.getUser(), target.getUser(), dateTime, noteContents);
                int noteID = storage.addNote(entry);
                channel.sendMessage(messages().MODERATION.addNote(performer, noteID, entry).build(getBot())).queue();
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
            channel.sendMessage(messages().GENERAL.guildOnlyCommand(ctx.getSource().getAuthor()).build(getBot())).queue();
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
                channel.sendMessage(messages().GENERAL.cannotActionSelf(performer).build(getBot())).queue();
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
        }

        final OffsetDateTime dateTime = OffsetDateTime.now();

        if (!performer.hasPermission(NOTE_PERMISSION))
            channel.sendMessage(
                messages().MODERATION.ERRORS.performerInsufficientPermissions(performer, NOTE_PERMISSION).build(getBot()))
                .queue();
        else
            channel.sendMessage(messages().MODERATION.noteList(
                NoteStorage.get(getBot().getStorage(), guild)
                    .getNotes()
                    .entrySet().stream()
                    .filter(predicate)
                    .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue))
            ).build(getBot())).queue();
        return 1;
    }

    private int removeNote(CommandContext<MessageReceivedEvent> ctx, int noteID) {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            channel.sendMessage(messages().GENERAL.guildOnlyCommand(ctx.getSource().getAuthor()).build(getBot())).queue();
            return 1;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());

        final OffsetDateTime dateTime = OffsetDateTime.now();

        if (!performer.hasPermission(NOTE_PERMISSION))
            channel.sendMessage(
                messages().MODERATION.ERRORS.performerInsufficientPermissions(performer, NOTE_PERMISSION).build(getBot()))
                .queue();
        else {
            final NoteStorage storage = NoteStorage.get(getBot().getStorage(), guild);
            @Nullable
            final NoteEntry entry = storage.getNote(noteID);
            if (entry == null)
                channel.sendMessage(messages().MODERATION.ERRORS.noNoteFound(performer, noteID).build(getBot())).queue();
            else {
                storage.removeNote(noteID);
                channel.sendMessage(messages().MODERATION.removeNote(performer, noteID, entry).build(getBot())).queue();
            }
        }
        return 1;
    }
}
