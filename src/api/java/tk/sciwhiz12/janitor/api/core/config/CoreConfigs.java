package tk.sciwhiz12.janitor.api.core.config;

public final class CoreConfigs {
    public static final ConfigNode<Boolean> ENABLE = new ConfigNode<>(
        "enable", () -> true,
        "Whether the bot is enabled for this guild.",
        "Can be used to temporarily disable the bot in emergency situations.");

    public static final ConfigNode<String> COMMAND_PREFIX = new ConfigNode<>(
        "commands.prefix", () -> "!",
        "The prefix for all commands.");

    private CoreConfigs() {}
}
