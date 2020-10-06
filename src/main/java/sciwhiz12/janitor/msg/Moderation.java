package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.moderation.notes.NoteEntry;
import sciwhiz12.janitor.moderation.warns.WarningEntry;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public final class Moderation {
    public static final int MODERATION_COLOR = 0xF1BD25;
    public static final String GAVEL_ICON_URL = "https://cdn.discordapp.com/attachments/738478941760782526" +
        "/760463743330549760/gavel.png";

    private final Messages messages;
    public final Errors ERRORS;

    Moderation(Messages messages) {
        this.messages = messages;
        ERRORS = new Errors();
    }

    public MessageBuilder moderation() {
        return messages.message()
            .embed(embed -> embed
                .setColor(MODERATION_COLOR)
                .setTimestamp(OffsetDateTime.now(ZoneOffset.UTC))
            );
    }

    public MessageBuilder moderation(String author) {
        return moderation()
            .embed(embed -> embed.setAuthor(author, null, GAVEL_ICON_URL));
    }

    public class Errors {
        private Errors() {}

        public MessageBuilder performerInsufficientPermissions(final Member performer, final EnumSet<Permission> permissions) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .with("required_permissions",
                    () -> permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")))
                .embed(embed -> embed
                    .setTitle("moderation.insufficient_permissions.title")
                    .setDescription("moderation.insufficient_permissions.description")
                )
                .field("moderation.insufficient_permissions.field.performer", true)
                .field("moderation.insufficient_permissions.field.required_permissions", true);
        }

        public MessageBuilder cannotInteract(final Member performer, final Member target) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .apply(builder -> messages.member(builder, "target", target))
                .embed(embed -> embed
                    .setTitle("moderation.cannot_interact.title")
                    .setDescription("moderation.cannot_interact.description")
                )
                .field("moderation.cannot_interact.field.performer", true)
                .field("moderation.cannot_interact.field.target", true);
        }

        public MessageBuilder cannotWarnMods(final Member performer, final Member target) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .apply(builder -> messages.member(builder, "target", target))
                .embed(embed -> embed
                    .setTitle("moderation.warn.cannot_warn_mods.title")
                    .setDescription("moderation.warn.cannot_warn_mods.description")
                )
                .field("moderation.warn.cannot_warn_mods.field.performer", true)
                .field("moderation.warn.cannot_warn_mods.field.target", true);
        }

        public MessageBuilder cannotRemoveHigherModerated(final Member performer, final int caseID, final WarningEntry entry) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .apply(builder -> warningEntry(builder, "warning_entry", caseID, entry))
                .embed(embed -> embed
                    .setTitle("moderation.unwarn.cannot_remove_higher_mod.title")
                    .setDescription("moderation.unwarn.cannot_remove_higher_mod.description")
                )
                .field("moderation.unwarn.cannot_remove_higher_mod.field.performer", true)
                .field("moderation.unwarn.cannot_remove_higher_mod.field.target", true);
        }

        public MessageBuilder maxAmountOfNotes(final Member performer, final Member target, final int amount) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .apply(builder -> messages.member(builder, "target", target))
                .with("notes_amount", () -> String.valueOf(amount))
                .embed(embed -> embed
                    .setTitle("moderation.note.max_amount_of_notes.title")
                    .setDescription("moderation.note.max_amount_of_notes.description")
                )
                .field("moderation.note.max_amount_of_notes.field.performer", true)
                .field("moderation.note.max_amount_of_notes.field.target", true)
                .field("moderation.note.max_amount_of_notes.field.amount", true);
        }

        public MessageBuilder noNoteFound(final Member performer, final int noteID) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .with("note_id", () -> String.valueOf(noteID))
                .embed(embed -> embed
                    .setTitle("moderation.note.no_note_found.title")
                    .setDescription("moderation.note.no_note_found.description")
                )
                .field("moderation.note.no_note_found.field.performer", true)
                .field("moderation.note.no_note_found.field.note_id", true);
        }

        public MessageBuilder noWarnWithID(final Member performer, final int caseID) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .with("case_id", () -> String.valueOf(caseID))
                .embed(embed -> embed
                    .setTitle("moderation.unwarn.no_case_found.title")
                    .setDescription("moderation.unwarn.no_case_found.description")
                )
                .field("moderation.unwarn.no_case_found.field.performer", true)
                .field("moderation.unwarn.no_case_found.field.note_id", true);
        }

        public MessageBuilder cannotUnwarnSelf(final Member performer, final int caseID, final WarningEntry entry) {
            return messages.failure()
                .apply(builder -> messages.member(builder, "performer", performer))
                .apply(builder -> warningEntry(builder, "warning_entry", caseID, entry))
                .embed(embed -> embed
                    .setTitle("moderation.unwarn.cannot_unwarn_self.title")
                    .setDescription("moderation.unwarn.cannot_unwarn_self.description")
                )
                .field("moderation.unwarn.cannot_unwarn_self.field.performer", true)
                .field("moderation.unwarn.cannot_unwarn_self.field.original_performer", true)
                .field("moderation.unwarn.cannot_unwarn_self.field.target", true);
        }
    }

    public MessageBuilder kickUser(final Member performer, final Member target, final @Nullable String reason,
        final boolean sentDM) {
        return moderation("moderation.kick.info.author")
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> messages.member(builder, "target", target))
            .with("reason", () -> reason)
            .field("moderation.kick.info.field.performer", true)
            .field("moderation.kick.info.field.target", true)
            .field("moderation.kick.info.field.private_message." + (sentDM ? "sent" : "unsent"), true)
            .field("moderation.kick.info.field.reason", true);
    }

    public MessageBuilder kickedDM(final Member performer, final Member target, final @Nullable String reason) {
        return moderation()
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> messages.member(builder, "target", target))
            .with("reason", () -> reason)
            .embed(embed -> embed
                .setTitle("moderation.kick.dm.title")
                .setAuthor("moderation.kick.dm.author", null, performer.getGuild().getIconUrl())
            )
            .field("moderation.kick.dm.field.performer", true)
            .field("moderation.kick.dm.field.reason", true);
    }

    public MessageBuilder banUser(final Member performer, final Member target, final @Nullable String reason,
        final int deletionDays, final boolean sentDM) {
        return moderation("moderation.ban.info.author")
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> messages.member(builder, "target", target))
            .with("delete_duration", () -> String.valueOf(deletionDays))
            .with("reason", () -> reason)
            .field("moderation.ban.info.field.performer", true)
            .field("moderation.ban.info.field.target", true)
            .field("moderation.ban.info.field.private_message." + (sentDM ? "sent" : "unsent"), true)
            .field("moderation.ban.info.field.delete_duration", true)
            .field("moderation.ban.info.field.reason", true);
    }

    public MessageBuilder bannedDM(final Member performer, final Member target, @Nullable final String reason) {
        return moderation()
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> messages.member(builder, "target", target))
            .with("reason", () -> reason)
            .embed(embed -> embed
                .setTitle("moderation.ban.dm.title")
                .setAuthor("moderation.ban.dm.author", null, performer.getGuild().getIconUrl())
            )
            .field("moderation.ban.dm.field.performer", true)
            .field("moderation.ban.dm.field.reason", true);
    }

    public MessageBuilder unbanUser(final Member performer, final User target) {
        return moderation("moderation.unban.info.author")
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> messages.user(builder, "target", target))
            .field("moderation.unban.info.field.performer", true)
            .field("moderation.unban.info.field.target", true);
    }

    public void warningEntry(MessageBuilder builder, String head, int caseID, WarningEntry entry) {
        builder
            .with(head + ".case_id", () -> String.valueOf(caseID))
            .apply(b -> messages.user(b, head + ".performer", entry.getPerformer()))
            .apply(b -> messages.user(b, head + ".target", entry.getWarned()))
            .with(head + ".date_time", () -> entry.getDateTime().format(RFC_1123_DATE_TIME))
            .with(head + ".reason", entry::getReason);
    }

    public MessageBuilder warnUser(final Member performer, final int caseID, final WarningEntry entry, final boolean sentDM) {
        return moderation("moderation.warn.info.author")
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> warningEntry(builder, "warning_entry", caseID, entry))
            .field("moderation.warn.info.field.performer", true)
            .field("moderation.warn.info.field.target", true)
            .field("moderation.warn.info.field.private_message." + (sentDM ? "sent" : "unsent"), true)
            .field("moderation.warn.info.field.date_time", true)
            .field("moderation.warn.info.field.case_id", true)
            .field("moderation.warn.info.field.reason", true);
    }

    public MessageBuilder warnedDM(final Member performer, final Member target, final String reason,
        final OffsetDateTime dateTime) {
        return moderation()
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> messages.member(builder, "target", target))
            .with("date_time", () -> dateTime.format(RFC_1123_DATE_TIME))
            .with("reason", () -> reason)
            .embed(embed -> embed
                .setTitle("moderation.warn.dm.title")
                .setAuthor("moderation.warn.dm.author", null, performer.getGuild().getIconUrl())
            )
            .field("moderation.warn.dm.field.performer", true)
            .field("moderation.warn.dm.field.date_time", true)
            .field("moderation.warn.dm.field.reason", true);
    }

    public MessageBuilder warnList(final Map<Integer, WarningEntry> displayWarnings) {
        //        return channel.sendMessage(
        //            moderationEmbed(translate("moderation.warnlist.author"))
        //                .setDescription(displayWarnings.size() > 0 ? displayWarnings.entrySet().stream()
        //                    .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getKey)))
        //                    .limit(10)
        //                    .map(entry ->
        //                        translate("moderation.warnlist.entry",
        //                            entry.getKey(),
        //                            entry.getValue().getWarned().getAsMention(),
        //                            entry.getValue().getPerformer().getAsMention(),
        //                            entry.getValue().getDateTime().format(RFC_1123_DATE_TIME),
        //                            entry.getValue().getReason() != null
        //                                ? entry.getValue().getReason()
        //                                : translate("moderation.warnlist.entry.no_reason"))
        //                    )
        //                    .collect(Collectors.joining("\n"))
        //                    : translate("moderation.warnlist.empty"))
        //                .build()
        //        );
        return moderation()
            .embed(embed -> embed.setTitle("NO OP, CURRENTLY IN PROGRESS"));
    }

    public MessageBuilder unwarn(final Member performer, final int caseID, final WarningEntry entry) {
        return moderation("moderation.unwarn.author")
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> warningEntry(builder, "warning_entry", caseID, entry))
            .field("moderation.unwarn.field.performer", true)
            .field("moderation.unwarn.field.case_id", true)
            .field("moderation.unwarn.field.original_performer", true)
            .field("moderation.unwarn.field.original_target", true)
            .field("moderation.unwarn.field.date_time", true)
            .field("moderation.unwarn.field.reason", true);
    }

    public void noteEntry(MessageBuilder builder, String head, int noteID, NoteEntry entry) {
        builder
            .with(head + ".note_id", () -> String.valueOf(noteID))
            .apply(b -> messages.user(b, head + ".performer", entry.getPerformer()))
            .apply(b -> messages.user(b, head + ".target", entry.getTarget()))
            .with(head + ".date_time", () -> entry.getDateTime().format(RFC_1123_DATE_TIME))
            .with(head + ".contents", entry::getContents);
    }

    public MessageBuilder addNote(final Member performer, final int noteID, final NoteEntry entry) {
        return moderation("moderation.note.add.author")
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> noteEntry(builder, "note", noteID, entry))
            .field("moderation.note.add.field.performer", true)
            .field("moderation.note.add.field.target", true)
            .field("moderation.note.add.field.note_id", true)
            .field("moderation.note.add.field.date_time", true)
            .field("moderation.note.add.field.contents", true);
    }

    public MessageBuilder noteList(final Map<Integer, NoteEntry> displayNotes) {
        //        return channel.sendMessage(moderationEmbed(translate("moderation.note.list.author"))
        //            .setDescription(displayNotes.size() > 0 ? displayNotes.entrySet().stream()
        //                .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getKey)))
        //                .limit(10)
        //                .map(entry ->
        //                    translate("moderation.note.list.entry",
        //                        entry.getKey(),
        //                        entry.getValue().getTarget().getAsMention(),
        //                        entry.getValue().getPerformer().getAsMention(),
        //                        entry.getValue().getDateTime().format(RFC_1123_DATE_TIME),
        //                        entry.getValue().getContents())
        //                )
        //                .collect(Collectors.joining("\n"))
        //                : translate("moderation.note.list.empty"))
        //            .build()
        //        );
        return moderation()
            .embed(embed -> embed.setTitle("NO OP, CURRENTLY IN PROGRESS"));
    }

    public MessageBuilder removeNote(final Member performer, final int noteID, final NoteEntry entry) {
        return moderation("moderation.note.remove.author")
            .apply(builder -> messages.member(builder, "performer", performer))
            .apply(builder -> noteEntry(builder, "note", noteID, entry))
            .field("moderation.note.remove.field.performer", true)
            .field("moderation.note.remove.field.case_id", true)
            .field("moderation.note.remove.field.original_performer", true)
            .field("moderation.note.remove.field.original_target", true)
            .field("moderation.note.remove.field.date_time", true)
            .field("moderation.note.remove.field.contents", true);
    }
}
