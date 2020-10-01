package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.moderation.notes.NoteEntry;
import sciwhiz12.janitor.moderation.warns.WarningEntry;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
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

    private String translate(String key, Object... args) {
        return messages.translate(key, args);
    }

    public EmbedBuilder moderationEmbed() {
        return new EmbedBuilder()
            .setColor(MODERATION_COLOR)
            .setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public EmbedBuilder moderationEmbed(String author) {
        return moderationEmbed()
            .setAuthor(author, null, GAVEL_ICON_URL);
    }

    public class Errors {
        private Errors() {}

        public MessageAction performerInsufficientPermissions(MessageChannel channel, Member performer,
            EnumSet<Permission> permissions) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.insufficient_permissions.title"))
                    .setDescription(translate("moderation.insufficient_permissions.desc"))
                    .addField(
                        translate("moderation.insufficient_permissions.field.performer"),
                        performer.getAsMention(),
                        true)
                    .addField(new MessageEmbed.Field(
                        translate("moderation.insufficient_permissions.field.permissions"),
                        permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")), true))
                    .build()
            );
        }

        public MessageAction cannotModerate(MessageChannel channel, Member performer, Member target) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.cannot_interact.title"))
                    .setDescription(translate("moderation.cannot_interact.desc"))
                    .addField(translate("moderation.cannot_interact.field.performer"), performer.getAsMention(), true)
                    .addField(translate("moderation.cannot_interact.field.target"), target.getAsMention(), true)
                    .build()
            );
        }

        public MessageAction cannotWarnMods(MessageChannel channel, Member performer, Member target) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.warn.cannot_warn_mods.title"))
                    .setDescription(translate("moderation.warn.cannot_warn_mods.desc"))
                    .addField(translate("moderation.warn.cannot_warn_mods.field.performer"), performer.getAsMention(), true)
                    .addField(translate("moderation.warn.cannot_warn_mods.field.target"), target.getAsMention(), true).build()
            );
        }

        public MessageAction cannotRemoveHigherModerated(MessageChannel channel, Member performer, int caseID,
            WarningEntry entry) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.unwarn.cannot_remove_higher_mod.title"))
                    .setDescription(translate("moderation.unwarn.cannot_remove_higher_mod.desc"))
                    .addField(translate("moderation.unwarn.cannot_remove_higher_mod.field.performer"), performer.getAsMention(),
                        true)
                    .addField(translate("moderation.unwarn.cannot_remove_higher_mod.field.original_performer"),
                        entry.getPerformer().getAsMention(), true)
                    .addField(translate("moderation.unwarn.cannot_remove_higher_mod.field.case_id"), String.valueOf(caseID),
                        true)
                    .build()
            );
        }

        public MessageAction maxAmountOfNotes(MessageChannel channel, Member performer, Member target, int amount) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.note.max_amount_of_notes.title"))
                    .setDescription(translate("moderation.note.max_amount_of_notes.desc"))
                    .addField(translate("moderation.note.max_amount_of_notes.field.performer"), performer.getAsMention(), true)
                    .addField(translate("moderation.note.max_amount_of_notes.field.target"), target.getAsMention(), true)
                    .addField(translate("moderation.note.max_amount_of_notes.field.amount"), String.valueOf(amount), true)
                    .build()
            );
        }

        public MessageAction noNoteFound(MessageChannel channel, Member performer, int noteID) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.note.no_note_found.title"))
                    .setDescription(translate("moderation.note.no_note_found.desc"))
                    .addField(translate("moderation.note.no_note_found.field.performer"), performer.getAsMention(), true)
                    .addField(translate("moderation.note.no_note_found.field.note_id"), String.valueOf(noteID), true)
                    .build()
            );
        }

        public MessageAction noWarnWithID(MessageChannel channel, Member performer, int caseID) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.unwarn.no_case_found.title"))
                    .setDescription(translate("moderation.unwarn.no_case_found.desc"))
                    .addField(translate("moderation.unwarn.no_case_found.field.performer"), performer.getAsMention(), true)
                    .addField(translate("moderation.unwarn.no_case_found.field.case_id"), String.valueOf(caseID), true).build()
            );
        }

        public MessageAction cannotUnwarnSelf(MessageChannel channel, Member performer, int caseID, WarningEntry entry) {
            return channel.sendMessage(
                messages.failureEmbed(translate("moderation.unwarn.cannot_unwarn_self.title"))
                    .setDescription(translate("moderation.unwarn.cannot_unwarn_self.desc"))
                    .addField(translate("moderation.unwarn.cannot_unwarn_self.field.performer"), performer.getAsMention(),
                        true)
                    .addField(translate("moderation.unwarn.cannot_unwarn_self.field.original_performer"),
                        entry.getPerformer().getAsMention(), true)
                    .addField(translate("moderation.unwarn.cannot_unwarn_self.field.case_id"), String.valueOf(caseID), true)
                    .build()
            );
        }
    }

    public MessageAction kickUser(MessageChannel channel, Member performer, Member target, @Nullable String reason,
        boolean sentDM) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.kick.info.author"))
                .addField(translate("moderation.kick.info.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.kick.info.field.target"), target.getAsMention(), true)
                .addField(translate("moderation.kick.info.field.sent_private_message"), sentDM ? "✅" : "❌", true)
                .addField(reason != null ? translate("moderation.kick.info.field.reason") : null, reason, false)
                .build()
        );
    }

    public MessageAction kickedDM(MessageChannel channel, Member performer, Member target, @Nullable String reason) {
        return channel.sendMessage(
            moderationEmbed()
                .setAuthor(performer.getGuild().getName(), null, performer.getGuild().getIconUrl())
                .setTitle(translate("moderation.kick.dm.title"))
                .addField(translate("moderation.kick.dm.field.performer"), performer.getUser().getAsMention(), true)
                .addField(reason != null ? translate("moderation.kick.dm.field.reason") : null, reason, false)
                .build()
        );
    }

    public MessageAction banUser(MessageChannel channel, Member performer, Member target, @Nullable String reason,
        int deletionDays, boolean sentDM) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.ban.info.author"))
                .addField(translate("moderation.ban.info.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.ban.info.field.target"), target.getAsMention(), true)
                .addField(translate("moderation.ban.info.field.sent_private_message"), sentDM ? "✅" : "❌", true)
                .addField(deletionDays != 0 ?
                    new MessageEmbed.Field(translate("moderation.ban.info.field.delete_duration"),
                        translate("moderation.ban.info.field.delete_duration.value", String.valueOf(deletionDays)), true)
                    : null)
                .addField(reason != null ? translate("moderation.ban.info.field.reason") : null, reason, false)
                .build()
        );
    }

    public MessageAction bannedDM(MessageChannel channel, Member performer, @Nullable String reason) {
        return channel.sendMessage(
            moderationEmbed()
                .setAuthor(performer.getGuild().getName(), null, performer.getGuild().getIconUrl())
                .setTitle(translate("moderation.ban.dm.title"))
                .addField(translate("moderation.ban.dm.field.performer"), performer.getAsMention(), true)
                .addField(reason != null ? translate("moderation.ban.dm.field.reason") : null, reason, false)
                .build()
        );
    }

    public MessageAction unbanUser(MessageChannel channel, Member performer, User target) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.unban.info.author"))
                .addField(translate("moderation.unban.info.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.unban.info.field.target"), target.getAsMention(), true)
                .build()
        );
    }

    public MessageAction warnUser(MessageChannel channel, Member performer, Member target, String reason,
        OffsetDateTime dateTime, int caseID, boolean sentDM) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.warn.info.author"))
                .addField(translate("moderation.warn.info.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.warn.info.field.target"), target.getAsMention(), true)
                .addField(translate("moderation.warn.info.field.sent_private_message"), sentDM ? "✅" : "❌", true)
                .addField(translate("moderation.warn.info.field.case_id"), String.valueOf(caseID), true)
                .addField(translate("moderation.warn.info.field.date_time"),
                    dateTime.format(RFC_1123_DATE_TIME), true)
                .addField(translate("moderation.warn.info.field.reason"), reason, false).build()
        );
    }

    public MessageAction warnDM(MessageChannel channel, Member performer, Member target, String reason,
        OffsetDateTime dateTime) {
        return channel.sendMessage(
            moderationEmbed()
                .setAuthor(performer.getGuild().getName(), null, performer.getGuild().getIconUrl())
                .setTitle(translate("moderation.warn.dm.title"))
                .addField(translate("moderation.warn.dm.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.warn.dm.field.date_time"),
                    dateTime.format(RFC_1123_DATE_TIME), true)
                .addField(translate("moderation.warn.dm.field.reason"), reason, false)
                .build()
        );
    }

    public MessageAction warnList(MessageChannel channel, Map<Integer, WarningEntry> displayWarnings) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.warnlist.author"))
                .setDescription(displayWarnings.size() > 0 ? displayWarnings.entrySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getKey)))
                    .limit(10)
                    .map(entry ->
                        translate("moderation.warnlist.entry",
                            entry.getKey(),
                            entry.getValue().getWarned().getAsMention(),
                            entry.getValue().getPerformer().getAsMention(),
                            entry.getValue().getDateTime().format(RFC_1123_DATE_TIME),
                            entry.getValue().getReason() != null
                                ? entry.getValue().getReason()
                                : translate("moderation.warnlist.entry.no_reason"))
                    )
                    .collect(Collectors.joining("\n"))
                    : translate("moderation.warnlist.empty"))
                .build()
        );
    }

    public MessageAction unwarn(MessageChannel channel, Member performer, int caseID, WarningEntry entry) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.unwarn.author"))
                .addField(translate("moderation.unwarn.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.unwarn.field.case_id"), String.valueOf(caseID), true)
                .addField(translate("moderation.unwarn.field.original_target"), entry.getWarned().getAsMention(), true)
                .addField(translate("moderation.unwarn.field.original_performer"), entry.getPerformer().getAsMention(), true)
                .addField(translate("moderation.unwarn.field.date_time"), entry.getDateTime().format(RFC_1123_DATE_TIME), true)
                .addField(entry.getReason() != null ? translate("moderation.unwarn.field.reason") : null, entry.getReason(),
                    false)
                .build()
        );
    }

    public MessageAction addNote(MessageChannel channel, Member performer, Member target, String contents,
        OffsetDateTime dateTime, int noteID) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.note.add.author"))
                .addField(translate("moderation.note.add.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.note.add.field.target"), target.getUser().getAsMention(), true)
                .addField(translate("moderation.note.add.field.note_id"), String.valueOf(noteID), true)
                .addField(translate("moderation.note.add.field.date_time"), dateTime.format(RFC_1123_DATE_TIME), true)
                .addField(translate("moderation.note.add.field.contents"), contents, false)
                .build()
        );
    }

    public MessageAction noteList(MessageChannel channel, Map<Integer, NoteEntry> displayNotes) {
        return channel.sendMessage(moderationEmbed(translate("moderation.note.list.author"))
            .setDescription(displayNotes.size() > 0 ? displayNotes.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getKey)))
                .limit(10)
                .map(entry ->
                    translate("moderation.note.list.entry",
                        entry.getKey(),
                        entry.getValue().getTarget().getAsMention(),
                        entry.getValue().getPerformer().getAsMention(),
                        entry.getValue().getDateTime().format(RFC_1123_DATE_TIME),
                        entry.getValue().getContents())
                )
                .collect(Collectors.joining("\n"))
                : translate("moderation.note.list.empty"))
            .build()
        );
    }

    public MessageAction removeNote(MessageChannel channel, Member performer, int noteID, NoteEntry entry) {
        return channel.sendMessage(
            moderationEmbed(translate("moderation.note.remove.author"))
                .addField(translate("moderation.note.remove.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.note.remove.field.note_id"), String.valueOf(noteID), true)
                .addField(translate("moderation.note.remove.field.original_target"), entry.getTarget().getAsMention(), true)
                .addField(translate("moderation.note.remove.field.original_performer"), entry.getPerformer().getAsMention(),
                    true)
                .addField(translate("moderation.note.remove.field.date_time"), entry.getDateTime().format(RFC_1123_DATE_TIME),
                    true)
                .addField(translate("moderation.note.remove.field.contents"), entry.getContents(), false)
                .build()
        );
    }
}
