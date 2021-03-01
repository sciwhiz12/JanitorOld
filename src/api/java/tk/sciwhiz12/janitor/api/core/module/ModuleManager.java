package tk.sciwhiz12.janitor.api.core.module;

import tk.sciwhiz12.janitor.api.JanitorBot;

import java.util.Set;
import javax.annotation.Nullable;

public interface ModuleManager {
    void disableModule(String id);

    void enableModule(String id);

    Set<ModuleKey<?>> getAvailableModules();

    Set<ModuleKey<?>> getActiveModules();

    @Nullable
    <M extends Module> M getModule(ModuleKey<M> moduleKey);

    boolean isActivated();

    JanitorBot getBot();
}
