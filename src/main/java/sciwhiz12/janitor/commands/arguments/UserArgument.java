package sciwhiz12.janitor.commands.arguments;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserArgument implements ArgumentType<UserArgument.IUserProvider> {
    public static final SimpleCommandExceptionType UNKNOWN_USER_IDENTIFIER = new SimpleCommandExceptionType(new LiteralMessage("Unknown user identifier"));
    public static final Pattern USER_IDENTIFIER_PATTERN = Pattern.compile("<@!?([0-9]+)>");

    public static UserArgument user() {
        return new UserArgument();
    }

    public static IUserProvider getUser(String name, CommandContext<?> ctx) {
        return ctx.getArgument(name, IUserProvider.class);
    }

    @Override
    public IUserProvider parse(StringReader reader) throws CommandSyntaxException {
        int startCursor = reader.getCursor();
        if (reader.peek() == '<') { // Expecting a possible user identifier
            int start = reader.getCursor();
            reader.readStringUntil('>');
            Matcher matcher = USER_IDENTIFIER_PATTERN.matcher(reader.getString().substring(start, reader.getCursor()));
            if (matcher.matches()) {
                return new NumericalProvider(Long.parseLong(matcher.group(1)));
            }
        }
        throw UNKNOWN_USER_IDENTIFIER.create();
    }

    @Override
    public Collection<String> getExamples() {
        return ImmutableList.of("<@!607058472709652501>", "<@750291676764962816>");
    }

    public interface IUserProvider {
        CompletableFuture<User> getUsers(DiscordApi api);
    }

    static class NumericalProvider implements IUserProvider {
        private final long snowflakeID;

        NumericalProvider(long snowflakeID) {
            this.snowflakeID = snowflakeID;
        }

        @Override
        public CompletableFuture<User> getUsers(DiscordApi api) {
            return api.getUserById(snowflakeID);
        }
    }
}
