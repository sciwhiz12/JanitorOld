package tk.sciwhiz12.janitor.api.core.module;

import tk.sciwhiz12.janitor.api.JanitorBot;

import java.util.Set;
import javax.annotation.Nullable;

public interface ModuleProvider {
    Set<ModuleKey<?>> getAvailableModules();

    @Nullable
    <M extends Module> M createModule(ModuleKey<M> moduleID, JanitorBot bot);
}
