package sciwhiz12.janitor.moderation;

import sciwhiz12.janitor.api.config.ConfigNode;

public class ModerationConfigs {
    public static final ConfigNode<Boolean> ENABLE_WARNS = new ConfigNode<>(
        "sciwhiz12.janitor.moderation.warns.enable", () -> true,
        "Whether to enable the warnings system. If disabled, the related commands are force-disabled.");

    public static final ConfigNode<Boolean> WARNS_RESPECT_MOD_ROLES = new ConfigNode<>(
        "sciwhiz12.janitor.moderation.warns.respect_mod_roles", () -> false,
        "Whether to prevent lower-ranked moderators (in the role hierarchy) from removing warnings " +
            "issued by higher-ranked moderators.");
    public static final ConfigNode<Boolean> ALLOW_WARN_OTHER_MODERATORS = new ConfigNode<>(
        "sciwhiz12.janitor.moderation.warns.warn_other_moderators", () -> true,
        "Whether to allow moderators to issue warnings against other moderators.");
    public static final ConfigNode<Boolean> ALLOW_REMOVE_SELF_WARNINGS = new ConfigNode<>(
        "sciwhiz12.janitor.moderation.warns.remove_self_warnings", () -> false,
        "Whether to allow moderators to remove warnings from themselves.");

    public static final ConfigNode<Boolean> ENABLE_NOTES = new ConfigNode<>(
        "sciwhiz12.janitor.moderation.notes.enable", () -> true,
        "Whether to enable the notes system. If disabled, the related commands are force-disabled.");
    public static final ConfigNode<Integer> MAX_NOTES_PER_MOD = new ConfigNode<>(
        "sciwhiz12.janitor.moderation.notes.max_amount", () -> Integer.MAX_VALUE,
        "The max amount of notes for a user per moderator.");
}
