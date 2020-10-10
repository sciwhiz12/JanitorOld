package sciwhiz12.janitor.commands.moderation;

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
import sciwhiz12.janitor.moderation.notes.NoteEntry;
import sciwhiz12.janitor.moderation.notes.NoteStorage;
import sciwhiz12.janitor.msg.MessageHelper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel).queue();

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
                .send(getBot(), channel).queue();

        } else if (performer.equals(target)) {
            messages().getRegularMessage("general/error/cannot_action_performer")
                .apply(MessageHelper.member("performer", performer))
                .send(getBot(), channel).queue();

        } else if (!performer.hasPermission(NOTE_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", NOTE_PERMISSION::toString)
                .send(getBot(), channel).queue();

        } else {
            final NoteStorage storage = NoteStorage.get(getBot().getStorage(), guild);
            final int maxAmount = config().NOTES_MAX_AMOUNT_PER_MOD.get();
            if (storage.getAmountOfNotes(target.getUser()) >= maxAmount) {
                messages().getRegularMessage("moderation/error/insufficient_permissions")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.member("target", target))
                    .with("notes_amount", () -> String.valueOf(maxAmount))
                    .send(getBot(), channel).queue();

            } else {
                final NoteEntry entry = new NoteEntry(performer.getUser(), target.getUser(), dateTime, noteContents);
                int noteID = storage.addNote(entry);

                messages().getRegularMessage("moderation/note/add")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.noteEntry("note_entry", noteID, entry))
                    .send(getBot(), channel).queue();

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
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel).queue();

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
                    .send(getBot(), channel).queue();

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
                .send(getBot(), channel).queue();

        } else {
            //            channel.sendMessage(messages().MODERATION.noteList(
            //                NoteStorage.get(getBot().getStorage(), guild)
            //                    .getNotes()
            //                    .entrySet().stream()
            //                    .filter(predicate)
            //                    .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue))
            //            ).build(getBot())).queue();
            messages().getRegularMessage("moderation/note/list")
                .send(getBot(), channel).queue();
            // TODO: fix this
        }
        return 1;
    }

    private int removeNote(CommandContext<MessageReceivedEvent> ctx, int noteID) {
        MessageChannel channel = ctx.getSource().getChannel();
        if (!ctx.getSource().isFromGuild()) {
            messages().getRegularMessage("general/error/guild_only_command")
                .apply(MessageHelper.user("performer", ctx.getSource().getAuthor()))
                .send(getBot(), channel).queue();

            return 1;
        }
        final Guild guild = ctx.getSource().getGuild();
        final Member performer = Objects.requireNonNull(ctx.getSource().getMember());

        if (!performer.hasPermission(NOTE_PERMISSION)) {
            messages().getRegularMessage("moderation/error/insufficient_permissions")
                .apply(MessageHelper.member("performer", performer))
                .with("required_permissions", NOTE_PERMISSION::toString)
                .send(getBot(), channel).queue();

        } else {
            final NoteStorage storage = NoteStorage.get(getBot().getStorage(), guild);
            @Nullable
            final NoteEntry entry = storage.getNote(noteID);
            if (entry == null) {
                messages().getRegularMessage("moderation/note/add")
                    .apply(MessageHelper.member("performer", performer))
                    .with("note_id", () -> String.valueOf(noteID))
                    .send(getBot(), channel).queue();

            } else {
                storage.removeNote(noteID);

                messages().getRegularMessage("moderation/note/remove")
                    .apply(MessageHelper.member("performer", performer))
                    .apply(MessageHelper.noteEntry("note_entry", noteID, entry))
                    .send(getBot(), channel).queue();
            }
        }
        return 1;
    }
}
