package sciwhiz12.janitor.utils;

import net.dv8tion.jda.api.entities.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {
    public static <T> T make(Supplier<T> creator, Consumer<T> configurator) {
        T obj = creator.get();
        configurator.accept(obj);
        return obj;
    }

    public static String toString(final User user) {
        return String.format("{User,%s#%s}:%s", user.getName(), user.getDiscriminator(), getID(user));
    }

    public static String toString(final MessageChannel channel) {
        if (channel instanceof GuildChannel) {
            GuildChannel gc = (GuildChannel) channel;
            return String.format("[Channel:%s,%s@%s]%s", gc.getType(), gc.getName(), toString(gc.getGuild()), getID(channel));
        }
        return String.format("[Channel:%s,%s]:%s", channel.getType(), channel.getName(), getID(channel));
    }

    public static String toString(final Guild guild) {
        return String.format("(Guild:%s):%s", guild.getName(), getID(guild));
    }

    public static String getID(final ISnowflake snowflake) {
        String prefix = "?";
        if (snowflake instanceof User) {
            prefix = "@&";
        } else if (snowflake instanceof Role) {
            prefix = "@!";
        } else if (snowflake instanceof MessageChannel) {
            prefix = "#";
        }
        return String.format("<%s%s>", prefix, snowflake.getIdLong());
    }

    public static <Success, Error> BiConsumer<Success, Error> handle(final Consumer<Success> success,
                                                                     final Consumer<Error> exceptionally) {
        return (suc, ex) -> {
            if (ex == null) {
                success.accept(suc);
            } else {
                exceptionally.accept(ex);
            }
        };
    }
}
