package com.boomer1.goatindustries;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DependencyVerifier {

    private static final String SLIMEFUN_CLASS =
        "io.github.thebusybiscuit.slimefun4.implementation.Slimefun";

    private DependencyVerifier() {
    }

    public static boolean verify(JavaPlugin plugin) {
        Plugin slimefun = plugin.getServer().getPluginManager().getPlugin("Slimefun");
        if (slimefun == null || !slimefun.isEnabled()) {
            plugin.getLogger().severe("[G.O.A.T. Industries] Slimefun is missing or disabled; item checks cannot run.");
            return false;
        }

        Set<String> enabledItemIds = null;
        String registryFailure = null;
        try {
            enabledItemIds = readEnabledSlimefunItemIds(slimefun);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            registryFailure = exception.getClass().getSimpleName() + ": " + exception.getMessage();
            plugin.getLogger().log(Level.SEVERE,
                "[G.O.A.T. Industries] Could not read Slimefun's enabled-item registry. "
                    + "All 13 addon plugin checks will still run, but item verification is unavailable.",
                exception);
        }

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        List<String> missingAddons = new ArrayList<>();
        List<DependencyCatalog.ItemRequirement> missingItems = new ArrayList<>();
        int successfulAddons = 0;

        for (DependencyCatalog.AddonRequirement addon : DependencyCatalog.addons()) {
            Plugin dependency = pluginManager.getPlugin(addon.pluginName());
            boolean addonPluginEnabled = dependency != null && dependency.isEnabled();

            if (dependency == null) {
                missingAddons.add(addon.name() + " (plugin '" + addon.pluginName() + "' is not installed)");
            } else if (!dependency.isEnabled()) {
                missingAddons.add(addon.name() + " (plugin '" + addon.pluginName() + "' is installed but disabled)");
            }

            List<DependencyCatalog.ItemRequirement> addonItems =
                DependencyCatalog.itemsForAddon(addon.name());
            boolean allAddonItemsPresent = registryFailure == null;

            if (enabledItemIds != null) {
                for (DependencyCatalog.ItemRequirement item : addonItems) {
                    if (!enabledItemIds.contains(item.id())) {
                        missingItems.add(item);
                        allAddonItemsPresent = false;
                    }
                }
            }

            if (addonPluginEnabled && allAddonItemsPresent) {
                successfulAddons++;
                plugin.getLogger().info("[G.O.A.T. Industries] " + addon.name() + " successfully confirmed.");
            }
        }

        if (registryFailure != null) {
            plugin.getLogger().severe("[G.O.A.T. Industries] Item verification could not be completed: "
                + registryFailure);
        }

        if (!missingAddons.isEmpty() || !missingItems.isEmpty() || registryFailure != null) {
            plugin.getLogger().severe("[G.O.A.T. Industries] Dependency verification failed after checking all "
                + DependencyCatalog.addons().size() + " addons.");

            for (String missingAddon : missingAddons) {
                plugin.getLogger().severe("[G.O.A.T. Industries] Missing addon: " + missingAddon);
            }

            for (DependencyCatalog.ItemRequirement missingItem : missingItems) {
                plugin.getLogger().severe("[G.O.A.T. Industries] Missing item"
                    + " | addon=" + missingItem.addon()
                    + " | item=" + missingItem.displayName()
                    + " | id=" + missingItem.id());
            }

            plugin.getLogger().severe("[G.O.A.T. Industries] Successfully confirmed "
                + successfulAddons + " of " + DependencyCatalog.addons().size() + " addons.");
            plugin.getLogger().severe("[G.O.A.T. Industries] Disabling G.O.A.T. Industries.");
            return false;
        }

        plugin.getLogger().info("[G.O.A.T. Industries] All " + DependencyCatalog.addons().size()
            + " addons and all " + DependencyCatalog.items().size()
            + " required item IDs were successfully confirmed.");
        return true;
    }

    private static Set<String> readEnabledSlimefunItemIds(Plugin slimefun)
        throws ReflectiveOperationException {

        ClassLoader slimefunClassLoader = slimefun.getClass().getClassLoader();
        Class<?> slimefunClass = Class.forName(SLIMEFUN_CLASS, true, slimefunClassLoader);

        Method getRegistry = slimefunClass.getMethod("getRegistry");
        Object registry = getRegistry.invoke(null);

        Class<?> registryClass = Class.forName(
            "io.github.thebusybiscuit.slimefun4.core.SlimefunRegistry", true, slimefunClassLoader);
        Method getEnabledItems = registryClass.getMethod("getEnabledSlimefunItems");
        Object enabledItems = getEnabledItems.invoke(registry);

        if (!(enabledItems instanceof Iterable<?> iterable)) {
            throw new IllegalStateException("Slimefun returned an unexpected enabled-item registry type.");
        }

        Class<?> slimefunItemClass = Class.forName(
            "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem", true, slimefunClassLoader);
        Method getId = slimefunItemClass.getMethod("getId");

        Set<String> ids = new HashSet<>();
        for (Object item : iterable) {
            if (item == null) {
                continue;
            }

            Object id = getId.invoke(item);
            if (id instanceof String stringId && !stringId.isBlank()) {
                ids.add(stringId);
            }
        }

        return ids;
    }
}
