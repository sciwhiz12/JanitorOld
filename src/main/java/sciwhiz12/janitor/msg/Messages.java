package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.checkerframework.checker.nullness.qual.Nullable;
import sciwhiz12.janitor.JanitorBot;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static sciwhiz12.janitor.utils.Util.nameFor;

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
                    .build()
            );
        }

        public RestAction<Message> ambiguousMember(MessageChannel channel) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.ambiguous_member.title"))
                    .setDescription(translate("general.ambiguous_member.desc"))
                    .setColor(FAILURE_COLOR)
                    .build()
            );
        }

        public RestAction<Message> cannotInteract(MessageChannel channel, Member target) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("general.cannot_interact.title"))
                    .setDescription(translate("general.cannot_interact.desc"))
                    .addField(translate("general.cannot_interact.field.target"), nameFor(target.getUser()), true)
                    .setColor(General.FAILURE_COLOR)
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
                    .addField(translate("moderation.insufficient_permissions.field.performer"), nameFor(performer.getUser()),
                        true)
                    .addField(new MessageEmbed.Field(
                        translate("moderation.insufficient_permissions.field.permissions"),
                        permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")),
                        true))
                    .setColor(General.FAILURE_COLOR)
                    .build()
            );
        }

        public MessageAction cannotModerate(MessageChannel channel, Member performer, Member target) {
            return channel.sendMessage(
                new EmbedBuilder()
                    .setTitle(translate("moderation.cannot_interact.title"))
                    .setDescription(translate("moderation.cannot_interact.desc"))
                    .addField(translate("moderation.cannot_interact.field.performer"), nameFor(performer.getUser()), true)
                    .addField(translate("moderation.cannot_interact.field.target"), nameFor(target.getUser()), true)
                    .setColor(General.FAILURE_COLOR)
                    .build()
            );
        }

        public MessageAction kickUser(MessageChannel channel, Member performer, Member target, @Nullable String reason) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(translate("moderation.kick.info.author"), null, GAVEL_ICON_URL)
                .addField(translate("moderation.kick.info.field.performer"), nameFor(performer.getUser()), true)
                .addField(translate("moderation.kick.info.field.target"), nameFor(target.getUser()), true);
            if (reason != null)
                embed.addField(translate("moderation.kick.info.field.reason"), reason, false);
            return channel.sendMessage(embed.setColor(MODERATION_COLOR).build());
        }

        public MessageAction kickedDM(MessageChannel channel, Member performer, Member target, @Nullable String reason) {
            final EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(performer.getGuild().getName(), null, performer.getGuild().getIconUrl())
                .setTitle(translate("moderation.kick.dm.title"))
                .addField(translate("moderation.kick.dm.field.performer"), nameFor(performer.getUser()), true);
            if (reason != null)
                embed.addField(translate("moderation.kick.dm.field.reason"), reason, false);
            return channel.sendMessage(embed.setColor(MODERATION_COLOR).build());
        }
    }

}
