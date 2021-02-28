package tk.sciwhiz12.janitor.api.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import tk.sciwhiz12.janitor.api.utils.StringReaderUtil;

import java.util.Collection;

public class CustomStringArgumentType implements ArgumentType<String> {
    private final StringArgumentType.StringType type;

    private CustomStringArgumentType(final StringArgumentType.StringType type) {
        this.type = type;
    }

    public static CustomStringArgumentType word() {
        return new CustomStringArgumentType(StringArgumentType.StringType.SINGLE_WORD);
    }

    public static CustomStringArgumentType string() {
        return new CustomStringArgumentType(StringArgumentType.StringType.QUOTABLE_PHRASE);
    }

    public static CustomStringArgumentType greedyString() {
        return new CustomStringArgumentType(StringArgumentType.StringType.GREEDY_PHRASE);
    }

    public static String getString(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    public StringArgumentType.StringType getType() {
        return type;
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        if (type == StringArgumentType.StringType.GREEDY_PHRASE) {
            final String text = reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
            return text;
        } else if (type == StringArgumentType.StringType.SINGLE_WORD) {
            return StringReaderUtil.readUnquotedString(reader);
        } else {
            return StringReaderUtil.readString(reader);
        }
    }

    @Override
    public String toString() {
        return "string()";
    }

    @Override
    public Collection<String> getExamples() {
        return type.getExamples();
    }

    public static String escapeIfRequired(final String input) {
        for (final char c : input.toCharArray()) {
            if (!StringReader.isAllowedInUnquotedString(c)) {
                return escape(input);
            }
        }
        return input;
    }

    private static String escape(final String input) {
        final StringBuilder result = new StringBuilder("\"");

        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == '\\' || c == '"') {
                result.append('\\');
            }
            result.append(c);
        }

        result.append("\"");
        return result.toString();
    }
}
