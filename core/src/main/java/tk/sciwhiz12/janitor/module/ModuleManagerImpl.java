package tk.sciwhiz12.janitor.module;

import com.google.common.collect.ImmutableSet;
import tk.sciwhiz12.janitor.JanitorBotImpl;
import tk.sciwhiz12.janitor.api.module.Module;
import tk.sciwhiz12.janitor.api.module.ModuleKey;
import tk.sciwhiz12.janitor.api.module.ModuleManager;
import tk.sciwhiz12.janitor.api.module.ModuleProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.Nullable;

import static tk.sciwhiz12.janitor.api.Logging.JANITOR;
import static tk.sciwhiz12.janitor.api.Logging.MODULE;

public class ModuleManagerImpl implements ModuleManager {
    private final JanitorBotImpl bot;
    private final ServiceLoader<ModuleProvider> moduleProviders;
    private boolean active = false;
    private final Set<ModuleKey<?>> availableModules = new HashSet<>();
    private final Set<String> disabledModules = new HashSet<>();
    private final Map<String, InnerStorage<?>> activeModules = new HashMap<>();

    public ModuleManagerImpl(JanitorBotImpl bot) {
        this.bot = bot;
        this.moduleProviders = ServiceLoader.load(ModuleProvider.class);
        loadProviders();
    }

    public void loadProviders() {
        final Set<String> knownModules = new HashSet<>();
        moduleProviders.reload();
        availableModules.clear();
        for (ModuleProvider provider : moduleProviders) {
            for (ModuleKey<?> moduleID : provider.getAvailableModules()) {
                if (knownModules.contains(moduleID.getModuleID()))
                    throw new RuntimeException("Duplicate modules with module id " + moduleID);
                availableModules.add(moduleID);
                knownModules.add(moduleID.getModuleID());
            }
        }
    }

    public void activateModules() {
        if (active)
            throw new IllegalStateException("Modules are already activated");
        active = true;
        JANITOR.debug(MODULE, "Activating modules...");
        for (ModuleProvider provider : moduleProviders) {
            for (ModuleKey<?> moduleID : provider.getAvailableModules()) {
                String providerName = provider.getClass().getName();
                if (disabledModules.contains(moduleID.getModuleID())) {
                    JANITOR.debug(MODULE, "Module with ID {} from provider {} is disabled, skipping", moduleID, providerName);
                } else if (!availableModules.contains(moduleID)) {
                    JANITOR
                        .debug(MODULE, "Module with ID {} from provider {} was not previously available at loading, skipping",
                            moduleID, providerName);
                } else {
                    if (activateModule(provider, moduleID)) {
                        JANITOR.debug(MODULE, "Module with ID {} from provider {} is now activated", moduleID, providerName);
                    } else {
                        JANITOR.warn(MODULE,
                            "Module with ID {} was declared to be available by provider {}, but did not create a module; " +
                                "skipping",
                            moduleID, providerName);
                    }

                }
            }
        }
        JANITOR.info(MODULE, "Modules are now activated");
    }

    private <M extends Module> boolean activateModule(ModuleProvider provider, ModuleKey<M> moduleID) {
        final M module = provider.createModule(moduleID, bot);
        if (module == null) {
            return false;
        }
        module.activate();
        activeModules.put(moduleID.getModuleID(), new InnerStorage<>(moduleID, module));
        return true;
    }

    public void shutdown() {
        if (!active)
            throw new IllegalStateException("Modules are not activated");
    }

    @Override
    public void disableModule(String id) {
        if (active)
            throw new IllegalStateException("Cannot disable modules, as modules are already activated");
        disabledModules.add(id);
    }

    @Override
    public void enableModule(String id) {
        if (active)
            throw new IllegalStateException("Cannot reenable modules, as modules are already activated");
        disabledModules.remove(id);
    }

    @Override
    public Set<ModuleKey<?>> getAvailableModules() {
        return Collections.unmodifiableSet(availableModules);
    }

    @Override
    public Set<ModuleKey<?>> getActiveModules() {
        return activeModules.values().stream()
            .map(InnerStorage::getKey)
            .collect(ImmutableSet.toImmutableSet());
    }

    @Nullable
    @Override
    public <M extends Module> M getModule(ModuleKey<M> moduleKey) {
        if (activeModules.containsKey(moduleKey.getModuleID())) {
            return moduleKey.getType().cast(activeModules.get(moduleKey.getModuleID()).module);
        }
        return null;
    }

    public boolean isActivated() {
        return active;
    }

    @Override
    public JanitorBotImpl getBot() {
        return bot;
    }

    /**
     * <strong>For internal use only.</strong>
     */
    static class InnerStorage<M extends Module> {
        private final ModuleKey<M> key;
        private final M module;

        InnerStorage(ModuleKey<M> key, M storage) {
            this.key = key;
            this.module = storage;
        }

        public ModuleKey<M> getKey() {
            return key;
        }

        public M getModule() {
            return module;
        }
    }
}
