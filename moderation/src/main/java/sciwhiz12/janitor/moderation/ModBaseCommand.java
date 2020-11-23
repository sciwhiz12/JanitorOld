package sciwhiz12.janitor.moderation;

import net.dv8tion.jda.api.entities.Guild;
import sciwhiz12.janitor.api.command.BaseCommand;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.moderation.notes.NoteStorage;
import sciwhiz12.janitor.moderation.warns.WarningStorage;

public abstract class ModBaseCommand extends BaseCommand {
    protected ModerationModuleImpl module;

    public ModBaseCommand(ModerationModuleImpl module, CommandRegistry registry) {
        super(registry);
        this.module = module;
    }

    protected NoteStorage getNotes(Guild guild) {
        return module.getNotes(guild);
    }

    protected WarningStorage getWarns(Guild guild) {
        return module.getWarns(guild);
    }
}