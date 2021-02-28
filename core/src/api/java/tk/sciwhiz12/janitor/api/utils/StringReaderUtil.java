package tk.sciwhiz12.janitor.api.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import static com.mojang.brigadier.StringReader.isQuotedStringStart;

public final class StringReaderUtil {
    private StringReaderUtil() {}

    public static boolean isAllowedInUnquotedString(final char c) {
        return c >= '0' && c <= '9'
            || c >= 'A' && c <= 'Z'
            || c >= 'a' && c <= 'z'
            || c == '_' || c == '-'
            || c == '.' || c == '+'
            || c == '#' || c == '$';
        // TODO: only prevent whitespace and quotation marks
    }

    public static String readUnquotedString(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedInUnquotedString(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    public static String readQuotedString(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            return "";
        }
        final char next = reader.peek();
        if (!isQuotedStringStart(next)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(reader);
        }
        reader.skip();
        return readStringUntil(reader, next);
    }

    private static final char SYNTAX_ESCAPE = '\\';

    public static String readStringUntil(StringReader reader, char terminator) throws CommandSyntaxException {
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (reader.canRead()) {
            final char c = reader.read();
            if (escaped) {
                if (c == terminator || c == SYNTAX_ESCAPE) {
                    result.append(c);
                    escaped = false;
                } else {
                    reader.setCursor(reader.getCursor() - 1);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape()
                        .createWithContext(reader, String.valueOf(c));
                }
            } else if (c == SYNTAX_ESCAPE) {
                escaped = true;
            } else if (c == terminator) {
                return result.toString();
            } else {
                result.append(c);
            }
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(reader);
    }

    public static String readString(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            return "";
        }
        final char next = reader.peek();
        if (isQuotedStringStart(next)) {
            reader.skip();
            return readStringUntil(reader, next);
        }
        return readUnquotedString(reader);
    }
}
