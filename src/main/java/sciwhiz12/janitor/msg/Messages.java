package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import sciwhiz12.janitor.JanitorBot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class Messages {
    public static final int FAILURE_COLOR = 0xF73132;

    private final JanitorBot bot;
    public final General GENERAL;
    public final Moderation MODERATION;

    public Messages(JanitorBot bot) {
        this.bot = bot;
        this.GENERAL = new General(this);
        this.MODERATION = new Moderation(this);
    }

    public JanitorBot getBot() {
        return bot;
    }

    public MessageBuilder message() {
        final MessageBuilder builder = new MessageBuilder();
        builder.embed()
            .setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return builder;
    }

    public MessageBuilder failure() {
        final MessageBuilder builder = message();
        builder.embed()
            .setColor(FAILURE_COLOR);
        return builder;
    }

    public MessageBuilder snowflake(MessageBuilder builder, String head, ISnowflake snowflake) {
        return builder
            .with(head + ".id", snowflake::getId)
            .with(head + ".creation_datetime", () -> snowflake.getTimeCreated().format(RFC_1123_DATE_TIME));
    }

    public MessageBuilder mentionable(MessageBuilder builder, String head, IMentionable mentionable) {
        return builder
            .apply(b -> snowflake(b, head, mentionable))
            .with(head + ".mention", mentionable::getAsMention);
    }

    public MessageBuilder role(MessageBuilder builder, String head, Role role) {
        return builder
            .apply(b -> mentionable(b, head, role))
            .with(head + ".color_hex", () -> Integer.toHexString(role.getColorRaw()))
            .with(head + ".name", role::getName)
            .with(head + ".permissions", role.getPermissions()::toString);
    }

    public MessageBuilder user(MessageBuilder builder, String head, User user) {
        return builder
            .apply(b -> mentionable(b, head, user))
            .with(head + ".name", user::getName)
            .with(head + ".discriminator", user::getDiscriminator)
            .with(head + ".tag", user::getAsTag)
            .with(head + ".flags", user.getFlags()::toString);
    }

    public MessageBuilder guild(MessageBuilder builder, String head, Guild guild) {
        return builder
            .apply(b -> snowflake(b, head, guild))
            .with(head + ".name", guild::getName)
            .with(head + ".description", guild::getDescription)
            .with(head + ".voice_region", guild.getRegion()::toString)
            .with(head + ".boost.tier", guild.getBoostTier()::toString)
            .with(head + ".boost.count", () -> String.valueOf(guild.getBoostCount()))
            .with(head + ".locale", guild.getLocale()::toString)
            .with(head + ".verification_level", guild.getVerificationLevel()::toString);
    }

    public MessageBuilder member(MessageBuilder builder, String head, Member member) {
        return builder
            .apply(b -> user(b, head, member.getUser()))
            .apply(b -> guild(b, head + ".guild", member.getGuild()))
            .with(head + ".nickname", member::getNickname)
            .with(head + ".effective_name", member::getEffectiveName)
            .with(head + ".join_datetime", () -> member.getTimeJoined().format(RFC_1123_DATE_TIME))
            .with(head + ".color", () -> String.valueOf(member.getColorRaw()));
    }
}
