package sciwhiz12.janitor.utils;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {
    public static <T> T make(Supplier<T> creator, Consumer<T> configurator) {
        T obj = creator.get();
        configurator.accept(obj);
        return obj;
    }

    public static String toString(final MessageAuthor author) {
        return author.asUser().map(Util::toString).orElseGet(() -> String.format("{MessageAuthor,%s}:%s", author.getDiscriminatedName(), author.getId()));
    }

    public static String toString(@Nullable final User user) {
        return user != null ? String.format("{User,%s#%s}:%s", user.getName(), user.getDiscriminator(), getID(user)) : "unknown";
    }

    public static String toString(final Channel channel) {
        if (channel instanceof ServerChannel) {
            ServerChannel gc = (ServerChannel) channel;
            return String.format("[Channel:%s,%s@%s]%s", gc.getType(), gc.getName(), toString(gc.getServer()), getID(channel));
        }
        return String.format("[Channel:%s]:%s", channel.getType(), getID(channel));
    }

    public static String toString(final Server guild) {
        return String.format("(Guild:%s):%s", guild.getName(), getID(guild));
    }

    public static String getID(final DiscordEntity entity) {
        String prefix = "?";
        if (entity instanceof User) {
            prefix = "@&";
        } else if (entity instanceof Role) {
            prefix = "@!";
        } else if (entity instanceof Channel) {
            prefix = "#";
        }
        return String.format("<%s%s>", prefix, entity.getId());
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
