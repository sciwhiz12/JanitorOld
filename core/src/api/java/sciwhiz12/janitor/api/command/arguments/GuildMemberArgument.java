package sciwhiz12.janitor.api.command.arguments;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import sciwhiz12.janitor.api.utils.StringReaderUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GuildMemberArgument implements ArgumentType<GuildMemberArgument.IMemberProvider> {
    public static final SimpleCommandExceptionType UNKNOWN_MEMBER_IDENTIFIER = new SimpleCommandExceptionType(
        new LiteralMessage("Unknown user identifier"));
    public static final SimpleCommandExceptionType MULTIPLE_MEMBERS = new SimpleCommandExceptionType(
        new LiteralMessage("Too many users, when only one is needed"));

    public static final Pattern USER_IDENTIFIER_PATTERN = Pattern.compile("<@!?([0-9]+)>");

    public static GuildMemberArgument member() {
        return new GuildMemberArgument(false);
    }

    public static GuildMemberArgument members() {
        return new GuildMemberArgument(true);
    }

    public static IMemberProvider getMembers(String name, CommandContext<?> ctx) {
        return ctx.getArgument(name, IMemberProvider.class);
    }

    private final boolean allowMultiple;

    private GuildMemberArgument(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    @Override
    public IMemberProvider parse(StringReader reader) throws CommandSyntaxException {
        int startCursor = reader.getCursor();
        if (reader.peek() == '<') { // Expecting a possible user identifier
            int start = reader.getCursor();
            reader.readStringUntil('>');
            Matcher matcher = USER_IDENTIFIER_PATTERN.matcher(reader.getString().substring(start, reader.getCursor()));
            if (matcher.matches()) {
                return new NumericalProvider(Long.parseLong(matcher.group(1)));
            }
        }
        reader.setCursor(startCursor);
        if (StringReader.isAllowedNumber(reader.peek())) {
            try {
                long value = reader.readLong();
                return new NumericalProvider(value);
            } catch (CommandSyntaxException ignored) {
            }
        }
        try {
            return new NamedProvider(allowMultiple, StringReaderUtil.readString(reader));
        } catch (CommandSyntaxException ignored) {
        }
        throw UNKNOWN_MEMBER_IDENTIFIER.create();
    }

    @Override
    public String toString() {
        return "member()";
    }

    @Override
    public Collection<String> getExamples() {
        return ImmutableList.of("<@!607058472709652501>", "<@750291676764962816>");
    }

    public interface IMemberProvider {
        List<Member> fromGuild(Guild guild) throws CommandSyntaxException;
    }

    static class NumericalProvider implements IMemberProvider {
        private final long snowflakeID;

        NumericalProvider(long snowflakeID) {
            this.snowflakeID = snowflakeID;
        }

        @Override
        public List<Member> fromGuild(Guild guild) {
            final Member memberById = guild.getMemberById(snowflakeID);
            return memberById != null ? Collections.singletonList(memberById) : Collections.emptyList();
        }
    }

    static class NamedProvider implements IMemberProvider {
        private final boolean multiple;
        private final String name;

        NamedProvider(boolean multiple, String name) {
            this.multiple = multiple;
            this.name = name;
        }

        @Override
        public List<Member> fromGuild(Guild guild) throws CommandSyntaxException {
            final String nameLowercase = name.toLowerCase(Locale.ROOT);
            final List<Member> members = guild.getMembers().stream()
                .filter(member -> member.getUser().getAsTag().replaceAll("\\s", "").toLowerCase(Locale.ROOT)
                    .startsWith(nameLowercase))
                .collect(Collectors.toList());
            if (!multiple && members.size() > 1) {
                throw MULTIPLE_MEMBERS.create();
            }
            return members;
        }
    }
}
