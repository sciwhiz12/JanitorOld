package tk.sciwhiz12.janitor.moderation;

import com.google.common.collect.ImmutableSet;
import net.dv8tion.jda.api.entities.Guild;
import tk.sciwhiz12.janitor.api.JanitorBot;
import tk.sciwhiz12.janitor.api.core.module.Module;
import tk.sciwhiz12.janitor.api.core.module.ModuleKey;
import tk.sciwhiz12.janitor.api.core.module.ModuleProvider;
import tk.sciwhiz12.janitor.api.moderation.ModerationModule;
import tk.sciwhiz12.janitor.moderation.notes.NoteCommand;
import tk.sciwhiz12.janitor.api.moderation.notes.NoteStorage;
import tk.sciwhiz12.janitor.moderation.notes.NoteStorageImpl;
import tk.sciwhiz12.janitor.moderation.warns.UnwarnCommand;
import tk.sciwhiz12.janitor.moderation.warns.WarnCommand;
import tk.sciwhiz12.janitor.moderation.warns.WarnListCommand;
import tk.sciwhiz12.janitor.api.moderation.warns.WarningStorage;
import tk.sciwhiz12.janitor.moderation.warns.WarningStorageImpl;

import java.util.Set;
import javax.annotation.Nullable;

import static tk.sciwhiz12.janitor.moderation.ModerationConfigs.*;

public class ModerationModuleImpl implements ModerationModule {
    private final JanitorBot bot;

    ModerationModuleImpl(JanitorBot bot) {
        this.bot = bot;
    }

    @Override
    public void activate() {
        bot.getCommands().addCommand(reg -> new KickCommand(this, reg));
        bot.getCommands().addCommand(reg -> new BanCommand(this, reg));
        bot.getCommands().addCommand(reg -> new UnbanCommand(this, reg));
        bot.getCommands().addCommand(reg -> new WarnCommand(this, reg));
        bot.getCommands().addCommand(reg -> new WarnListCommand(this, reg));
        bot.getCommands().addCommand(reg -> new UnwarnCommand(this, reg));
        bot.getCommands().addCommand(reg -> new NoteCommand(this, reg));
        bot.getConfigs().registerNodes(
            ENABLE_WARNS,
            WARNS_RESPECT_MOD_ROLES,
            ALLOW_WARN_OTHER_MODERATORS,
            ALLOW_REMOVE_SELF_WARNINGS,
            ENABLE_NOTES,
            MAX_NOTES_PER_MOD
        );
        LOGGER.info("Moderation module is activated");
    }

    @Override
    public void shutdown() {}

    @Override
    public JanitorBot getBot() {
        return bot;
    }

    @Override
    public NoteStorage getNotes(Guild guild) {
        return bot.getGuildStorage().getOrCreate(guild, NoteStorage.KEY, () -> new NoteStorageImpl(bot));
    }

    @Override
    public WarningStorage getWarns(Guild guild) {
        return bot.getGuildStorage().getOrCreate(guild, WarningStorage.KEY, () -> new WarningStorageImpl(bot));
    }

    public static class Provider implements ModuleProvider {
        @Override
        public Set<ModuleKey<?>> getAvailableModules() {
            return ImmutableSet.of(ID);
        }

        @Nullable
        @Override
        public <M extends Module> M createModule(ModuleKey<M> moduleID, JanitorBot bot) {
            if (ID.equals(moduleID)) {
                //noinspection unchecked
                return (M) new ModerationModuleImpl(bot);
            }
            return null;
        }
    }
}
