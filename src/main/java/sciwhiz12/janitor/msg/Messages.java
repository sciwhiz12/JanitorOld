package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.moderation.notes.NoteEntry;
import sciwhiz12.janitor.moderation.warns.WarningEntry;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class Messages {
    private final JanitorBot bot;
    public final General GENERAL;
    public final Moderation MODERATION;

    public Messages(JanitorBot bot) {
        this.bot = bot;
        this.GENERAL = new General();
        this.MODERATION = new Moderation();
    }

    public String translate(String key, Object... args) {
        return bot.getTranslations().translate(key, args);
    }

    public final class General {
        public static final int FAILURE_COLOR = 0xF73132;

        private General() {}

        public RestAction<Message> guildOnlyCommand(MessageChannel channel) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.guild_only_command.title"))
                    .setDescription(translate("general.guild_only_command.desc"))
                    .setColor(FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public RestAction<Message> insufficientPermissions(MessageChannel channel, EnumSet<Permission> permissions) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.insufficient_permissions.title"))
                    .setDescription(translate("general.insufficient_permissions.desc"))
                    .addField(new MessageEmbed.Field(
                        translate("general.insufficient_permissions.field.permissions"),
                        permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")),
                        false))
                    .setColor(FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public RestAction<Message> ambiguousMember(MessageChannel channel) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.ambiguous_member.title"))
                    .setDescription(translate("general.ambiguous_member.desc"))
                    .setColor(FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public RestAction<Message> cannotInteract(MessageChannel channel, Member target) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.cannot_interact.title"))
                    .setDescription(translate("general.cannot_interact.desc"))
                    .addField(translate("general.cannot_interact.field.target"), target.getUser().getAsMention(), true)
                    .setColor(General.FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public RestAction<Message> cannotActionSelf(MessageChannel channel) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.cannot_action_self.title"))
                    .setDescription(translate("general.cannot_action_self.desc"))
                    .setColor(General.FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public RestAction<Message> cannotActionPerformer(MessageChannel channel, Member performer) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.cannot_action_performer.title"))
                    .setDescription(translate("general.cannot_action_performer.desc"))
                    .addField(translate("general.cannot_action_performer.field.performer"), performer.getUser().getAsMention(),
                        true)
                    .setColor(General.FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }
    }

    public final class Moderation {
        public static final int MODERATION_COLOR = 0xF1BD25;
        public static final String GAVEL_ICON_URL = "https://cdn.discordapp.com/attachments/738478941760782526" +
            "/760463743330549760/gavel.png";

        private Moderation() {}

        public MessageAction performerInsufficientPermissions(MessageChannel channel, Member performer,
            EnumSet<Permission> permissions) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("moderation.insufficient_permissions.title"))
                    .setDescription(translate("moderation.insufficient_permissions.desc"))
                    .addField(translate("moderation.insufficient_permissions.field.performer"),
                        performer.getUser().getAsMention(),
                        true)
                    .addField(new MessageEmbed.Field(
                        translate("moderation.insufficient_permissions.field.permissions"),
                        permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")),
                        true))
                    .setColor(General.FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public MessageAction cannotModerate(MessageChannel channel, Member performer, Member target) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("moderation.cannot_interact.title"))
                    .setDescription(translate("moderation.cannot_interact.desc"))
                    .addField(translate("moderation.cannot_interact.field.performer"), performer.getUser().getAsMention(), true)
                    .addField(translate("moderation.cannot_interact.field.target"), target.getUser().getAsMention(), true)
                    .setColor(General.FAILURE_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public MessageAction kickUser(MessageChannel channel, Member performer, Member target, @Nullable String reason,
            boolean sentDM) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.kick.info.author"), null, GAVEL_ICON_URL)
                .addField(translate("moderation.kick.info.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.kick.info.field.target"), target.getUser().getAsMention(), true)
                .addField(translate("moderation.kick.info.field.sent_private_message"), sentDM ? "✅" : "❌", true);
            if (reason != null)
                embed.addField(translate("moderation.kick.info.field.reason"), reason, false);
            return channel
                .sendMessage(embed.setColor(MODERATION_COLOR).setTimestamp(OffsetDateTime.now(Clock.systemUTC())).build());
        }

        public MessageAction kickedDM(MessageChannel channel, Member performer, Member target, @Nullable String reason) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(performer.getGuild().getName(), null, performer.getGuild().getIconUrl())
                .setTitle(translate("moderation.kick.dm.title"))
                .addField(translate("moderation.kick.dm.field.performer"), performer.getUser().getAsMention(), true);
            if (reason != null)
                embed.addField(translate("moderation.kick.dm.field.reason"), reason, false);
            return channel
                .sendMessage(embed.setColor(MODERATION_COLOR).setTimestamp(OffsetDateTime.now(Clock.systemUTC())).build());
        }

        public MessageAction banUser(MessageChannel channel, Member performer, Member target, @Nullable String reason,
            int deletionDays, boolean sentDM) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.ban.info.author"), null, GAVEL_ICON_URL)
                .addField(translate("moderation.ban.info.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.ban.info.field.target"), target.getUser().getAsMention(), true)
                .addField(translate("moderation.ban.info.field.sent_private_message"), sentDM ? "✅" : "❌", true);
            if (deletionDays != 0)
                embed.addField(translate("moderation.ban.info.field.delete_duration"),
                    String.valueOf(deletionDays).concat(" day(s)"), true);
            if (reason != null)
                embed.addField(translate("moderation.ban.info.field.reason"), reason, false);
            return channel
                .sendMessage(embed.setColor(MODERATION_COLOR).setTimestamp(OffsetDateTime.now(Clock.systemUTC())).build());
        }

        public MessageAction bannedDM(MessageChannel channel, Member performer, Member target, @Nullable String reason) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(performer.getGuild().getName(), null, performer.getGuild().getIconUrl())
                .setTitle(translate("moderation.ban.dm.title"))
                .addField(translate("moderation.ban.dm.field.performer"), performer.getUser().getAsMention(), true);
            if (reason != null)
                embed.addField(translate("moderation.ban.dm.field.reason"), reason, false);
            return channel
                .sendMessage(embed.setColor(MODERATION_COLOR).setTimestamp(OffsetDateTime.now(Clock.systemUTC())).build());
        }

        public MessageAction unbanUser(MessageChannel channel, Member performer, User target) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setAuthor(translate("moderation.unban.info.author"), null, GAVEL_ICON_URL)
                    .addField(translate("moderation.unban.info.field.performer"), performer.getUser().getAsMention(), true)
                    .addField(translate("moderation.unban.info.field.target"), target.getAsMention(), true)
                    .setColor(MODERATION_COLOR)
                    .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                    .build()
            );
        }

        public MessageAction warnUser(MessageChannel channel, Member performer, Member target, String reason,
            OffsetDateTime dateTime, int caseID, boolean sentDM) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.warn.info.author"), null, GAVEL_ICON_URL)
                .addField(translate("moderation.warn.info.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.warn.info.field.target"), target.getUser().getAsMention(), true)
                .addField(translate("moderation.warn.info.field.sent_private_message"), sentDM ? "✅" : "❌", true)
                .addField(translate("moderation.warn.info.field.case_id"), String.valueOf(caseID), true)
                .addField(translate("moderation.warn.info.field.date_time"),
                    dateTime.format(RFC_1123_DATE_TIME), true)
                .addField(translate("moderation.warn.info.field.reason"), reason, false);
            return channel
                .sendMessage(embed.setColor(MODERATION_COLOR).setTimestamp(OffsetDateTime.now(Clock.systemUTC())).build());
        }

        public MessageAction warnDM(MessageChannel channel, Member performer, Member target, String reason,
            OffsetDateTime dateTime) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(performer.getGuild().getName(), null, performer.getGuild().getIconUrl())
                .setTitle(translate("moderation.warn.dm.title"))
                .addField(translate("moderation.warn.dm.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.warn.dm.field.date_time"),
                    dateTime.format(RFC_1123_DATE_TIME), true)
                .addField(translate("moderation.warn.dm.field.reason"), reason, false);
            return channel.sendMessage(embed.build());
        }

        public MessageAction warnList(MessageChannel channel, Map<Integer, WarningEntry> displayWarnings) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.warnlist.author"), null, GAVEL_ICON_URL)
                .setColor(MODERATION_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()));
            String warningsDesc = displayWarnings.size() > 0 ? displayWarnings.entrySet().stream()
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
                : translate("moderation.warnlist.empty");
            embed.setDescription(warningsDesc);
            return channel.sendMessage(embed.build());
        }

        public MessageAction noWarnWithID(MessageChannel channel, Member performer, int caseID) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translate("moderation.unwarn.no_case_found.title"), null)
                .setColor(General.FAILURE_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setDescription(translate("moderation.unwarn.no_case_found.desc"))
                .addField(translate("moderation.unwarn.no_case_found.field.performer"), performer.getUser().getAsMention(),
                    true)
                .addField(translate("moderation.unwarn.no_case_found.field.case_id"), String.valueOf(caseID), true);
            return channel.sendMessage(embed.build());
        }

        public MessageAction cannotUnwarnSelf(MessageChannel channel, Member performer, int caseID, WarningEntry entry) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translate("moderation.unwarn.cannot_unwarn_self.title"), null)
                .setColor(General.FAILURE_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setDescription(translate("moderation.unwarn.cannot_unwarn_self.desc"))
                .addField(translate("moderation.unwarn.cannot_unwarn_self.field.performer"),
                    performer.getUser().getAsMention(), true)
                .addField(translate("moderation.unwarn.cannot_unwarn_self.field.original_performer"),
                    entry.getPerformer().getAsMention(), true)
                .addField(translate("moderation.unwarn.cannot_unwarn_self.field.case_id"), String.valueOf(caseID), true);
            return channel.sendMessage(embed.build());
        }

        public MessageAction unwarn(MessageChannel channel, Member performer, int caseID, WarningEntry entry) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.unwarn.author"), null, GAVEL_ICON_URL)
                .setColor(MODERATION_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .addField(translate("moderation.unwarn.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.unwarn.field.case_id"), String.valueOf(caseID), true)
                .addField(translate("moderation.unwarn.field.original_target"), entry.getWarned().getAsMention(), true)
                .addField(translate("moderation.unwarn.field.original_performer"), entry.getPerformer().getAsMention(),
                    true)
                .addField(translate("moderation.unwarn.field.date_time"),
                    entry.getDateTime().format(RFC_1123_DATE_TIME), true);
            if (entry.getReason() != null)
                embed.addField(translate("moderation.unwarn.field.reason"), entry.getReason(), false);
            return channel.sendMessage(embed.build());
        }

        public MessageAction cannotWarnMods(MessageChannel channel, Member performer, Member target) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translate("moderation.warn.cannot_warn_mods.title"), null)
                .setColor(General.FAILURE_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setDescription(translate("moderation.warn.cannot_warn_mods.desc"))
                .addField(translate("moderation.warn.cannot_warn_mods.field.performer"),
                    performer.getAsMention(), true)
                .addField(translate("moderation.warn.cannot_warn_mods.field.target"),
                    target.getAsMention(), true);
            return channel.sendMessage(embed.build());
        }

        public MessageAction cannotRemoveHigherModerated(MessageChannel channel, Member performer, int caseID,
            WarningEntry entry) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translate("moderation.unwarn.cannot_remove_higher_mod.title"), null)
                .setColor(General.FAILURE_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setDescription(translate("moderation.unwarn.cannot_remove_higher_mod.desc"))
                .addField(translate("moderation.unwarn.cannot_remove_higher_mod.field.performer"),
                    performer.getUser().getAsMention(), true)
                .addField(translate("moderation.unwarn.cannot_remove_higher_mod.field.original_performer"),
                    entry.getPerformer().getAsMention(), true)
                .addField(translate("moderation.unwarn.cannot_remove_higher_mod.field.case_id"), String.valueOf(caseID), true);
            return channel.sendMessage(embed.build());
        }

        public MessageAction maxAmountOfNotes(MessageChannel channel, Member performer, Member target, int amount) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translate("moderation.note.max_amount_of_notes.title"), null)
                .setColor(General.FAILURE_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setDescription(translate("moderation.note.max_amount_of_notes.desc"))
                .addField(translate("moderation.note.max_amount_of_notes.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.note.max_amount_of_notes.field.target"), target.getAsMention(), true)
                .addField(translate("moderation.note.max_amount_of_notes.field.amount"), String.valueOf(amount), true);
            return channel.sendMessage(embed.build());
        }

        public MessageAction noNoteFound(MessageChannel channel, Member performer, int noteID) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translate("moderation.note.no_note_found.title"), null)
                .setColor(General.FAILURE_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setDescription(translate("moderation.note.no_note_found.desc"))
                .addField(translate("moderation.note.no_note_found.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.note.no_note_found.field.note_id"), String.valueOf(noteID), true);
            return channel.sendMessage(embed.build());
        }

        public MessageAction addNote(MessageChannel channel, Member performer, Member target, String contents,
            OffsetDateTime dateTime, int noteID) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.note.add.author"), null, GAVEL_ICON_URL)
                .setColor(MODERATION_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .addField(translate("moderation.note.add.field.performer"), performer.getUser().getAsMention(), true)
                .addField(translate("moderation.note.add.field.target"), target.getUser().getAsMention(), true)
                .addField(translate("moderation.note.add.field.note_id"), String.valueOf(noteID), true)
                .addField(translate("moderation.note.add.field.date_time"), dateTime.format(RFC_1123_DATE_TIME), true)
                .addField(translate("moderation.note.add.field.contents"), contents, false);
            return channel.sendMessage(embed.build());
        }

        public MessageAction noteList(MessageChannel channel, Map<Integer, NoteEntry> displayNotes) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.note.list.author"), null, GAVEL_ICON_URL)
                .setColor(MODERATION_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()));
            String warningsDesc = displayNotes.size() > 0 ? displayNotes.entrySet().stream()
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
                : translate("moderation.note.list.empty");
            embed.setDescription(warningsDesc);
            return channel.sendMessage(embed.build());
        }

        public MessageAction removeNote(MessageChannel channel, Member performer, int noteID, NoteEntry entry) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.note.remove.author"), null, GAVEL_ICON_URL)
                .setColor(MODERATION_COLOR)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .addField(translate("moderation.note.remove.field.performer"), performer.getAsMention(), true)
                .addField(translate("moderation.note.remove.field.note_id"), String.valueOf(noteID), true)
                .addField(translate("moderation.note.remove.field.original_target"), entry.getTarget().getAsMention(), true)
                .addField(translate("moderation.note.remove.field.original_performer"), entry.getPerformer().getAsMention(),
                    true)
                .addField(translate("moderation.note.remove.field.date_time"), entry.getDateTime().format(RFC_1123_DATE_TIME),
                    true)
                .addField(translate("moderation.note.remove.field.contents"), entry.getContents(), false);
            return channel.sendMessage(embed.build());
        }
    }

}
