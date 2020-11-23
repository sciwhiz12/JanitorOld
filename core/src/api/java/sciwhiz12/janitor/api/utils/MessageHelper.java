package sciwhiz12.janitor.api.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import sciwhiz12.janitor.api.messages.substitution.ModifiableSubstitutions;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;
import java.util.function.Consumer;

import static java.time.temporal.ChronoField.*;

public final class MessageHelper {
    private MessageHelper() {}

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> snowflake(String head, ISnowflake snowflake) {
        return builder -> builder
            .with(head + ".id", snowflake::getId)
            .with(head + ".creation_datetime", () -> snowflake.getTimeCreated().format(DATE_TIME_FORMAT));
    }

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> mentionable(String head, IMentionable mentionable) {
        return builder -> builder
            .apply(snowflake(head, mentionable))
            .with(head + ".mention", mentionable::getAsMention);
    }

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> role(String head, Role role) {
        return builder -> builder
            .apply(mentionable(head, role))
            .with(head + ".color_hex", () -> Integer.toHexString(role.getColorRaw()))
            .with(head + ".name", role::getName)
            .with(head + ".permissions", role.getPermissions()::toString);
    }

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> user(String head, User user) {
        return builder -> builder
            .apply(mentionable(head, user))
            .with(head + ".name", user::getName)
            .with(head + ".discriminator", user::getDiscriminator)
            .with(head + ".tag", user::getAsTag)
            .with(head + ".flags", user.getFlags()::toString);
    }

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> guild(String head, Guild guild) {
        return builder -> builder
            .apply(snowflake(head, guild))
            .with(head + ".name", guild::getName)
            .with(head + ".description", guild::getDescription)
            .with(head + ".voice_region", guild.getRegion()::toString)
            .with(head + ".boost.tier", guild.getBoostTier()::toString)
            .with(head + ".boost.count", () -> String.valueOf(guild.getBoostCount()))
            .with(head + ".locale", guild.getLocale()::toString)
            .with(head + ".verification_level", guild.getVerificationLevel()::toString)
            .with(head + ".icon_url", () -> Objects.toString(guild.getIconUrl(), ""));
    }

    public static <T extends ModifiableSubstitutions<?>> Consumer<T> member(String head, Member member) {
        return builder -> builder
            .apply(user(head, member.getUser()))
            .apply(guild(head + ".guild", member.getGuild()))
            .with(head + ".nickname", member::getNickname)
            .with(head + ".effective_name", member::getEffectiveName)
            .with(head + ".join_datetime", () -> member.getTimeJoined().format(DATE_TIME_FORMAT))
            .with(head + ".color", () -> String.valueOf(member.getColorRaw()));
    }

    public static final DateTimeFormatter DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .parseLenient()
        .appendValue(YEAR, 4)  // 2 digit year not handled
        .appendLiteral('-')
        .appendValue(MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(DAY_OF_MONTH, 2)
        .appendLiteral(' ')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .optionalStart()
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .optionalEnd()
        .appendLiteral(' ')
        .appendOffset("+HHMM", "GMT")
        .toFormatter();
}
