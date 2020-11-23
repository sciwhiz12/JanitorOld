package sciwhiz12.janitor.api.module;

import sciwhiz12.janitor.api.JanitorBot;

import java.util.Set;
import javax.annotation.Nullable;

public interface ModuleProvider {
    Set<ModuleKey<?>> getAvailableModules();

    @Nullable
    <M extends Module> M createModule(ModuleKey<M> moduleID, JanitorBot bot);
}
