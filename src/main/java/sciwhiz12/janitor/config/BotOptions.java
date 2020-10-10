package sciwhiz12.janitor.config;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;

import java.nio.file.Path;
import java.util.Optional;

import static joptsimple.util.PathProperties.*;

public class BotOptions {
    private final OptionSet options;
    private final ArgumentAcceptingOptionSpec<Path> configPath;
    private final ArgumentAcceptingOptionSpec<Path> translationsPath;
    private final ArgumentAcceptingOptionSpec<Path> messagesFolder;
    private final ArgumentAcceptingOptionSpec<String> token;
    private final ArgumentAcceptingOptionSpec<String> prefix;
    private final ArgumentAcceptingOptionSpec<Long> owner;

    public BotOptions(String[] arguments) {
        OptionParser parser = new OptionParser();
        this.configPath = parser
            .accepts("config", "The path to the config file; defaults to 'config.toml'")
            .withRequiredArg()
            .withValuesConvertedBy(new PathConverter(FILE_EXISTING, READABLE, WRITABLE));
        this.translationsPath = parser
            .accepts("translations", "The path to the translations file")
            .withRequiredArg()
            .withValuesConvertedBy(new PathConverter(FILE_EXISTING, READABLE));
        this.messagesFolder = parser
            .accepts("translations", "The path to the custom messages folder")
            .withRequiredArg()
            .withValuesConvertedBy(new PathConverter(DIRECTORY_EXISTING, READABLE));
        this.token = parser
            .accepts("token", "The Discord token for the bot user")
            .withRequiredArg();
        this.prefix = parser
            .accepts("prefix", "The prefix for commands")
            .withRequiredArg();
        this.owner = parser.accepts("owner",
            "The user ID of the bot owner")
            .withRequiredArg()
            .ofType(Long.class);
        this.options = parser.parse(arguments);
    }

    public Optional<Path> getConfigPath() {
        return configPath.valueOptional(options);
    }

    public Optional<Path> getTranslationsFile() {
        return translationsPath.valueOptional(options);
    }

    public Optional<Path> getMessagesFolder() {
        return messagesFolder.valueOptional(options);
    }

    public Optional<String> getToken() {
        return token.valueOptional(options);
    }

    public Optional<String> getCommandPrefix() {
        return prefix.valueOptional(options);
    }

    public Optional<Long> getOwnerID() {
        return owner.valueOptional(options);
    }
}
