package tk.sciwhiz12.janitor.moderation;

import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.sciwhiz12.janitor.api.module.Module;
import tk.sciwhiz12.janitor.api.module.ModuleKey;
import tk.sciwhiz12.janitor.moderation.notes.NoteStorage;
import tk.sciwhiz12.janitor.moderation.warns.WarningStorage;

public interface ModerationModule extends Module {
    Logger LOGGER = LoggerFactory.getLogger("janitor.moderation");
    ModuleKey<ModerationModule> ID = new ModuleKey<>("moderation", "Moderation", ModerationModule.class);

    NoteStorage getNotes(Guild guild);

    WarningStorage getWarns(Guild guild);
}
