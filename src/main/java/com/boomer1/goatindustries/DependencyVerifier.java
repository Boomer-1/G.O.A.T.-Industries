package com.boomer1.goatindustries;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DependencyVerifier {

    private static final String SLIMEFUN_CLASS =
        "io.github.thebusybiscuit.slimefun4.implementation.Slimefun";

    private DependencyVerifier() {
    }

    public static boolean verify(JavaPlugin plugin) {
        boolean passed = true;

        Plugin emctech = plugin.getServer().getPluginManager().getPlugin(DependencyCatalog.REQUIRED_PLUGIN);
        if (emctech == null) {
            plugin.getLogger().severe("[G.O.A.T. Industries] Required plugin is not installed: "
                + DependencyCatalog.REQUIRED_PLUGIN);
            passed = false;
        } else if (!emctech.isEnabled()) {
            plugin.getLogger().severe("[G.O.A.T. Industries] Required plugin is installed but not enabled: "
                + DependencyCatalog.REQUIRED_PLUGIN);
            passed = false;
        } else {
            plugin.getLogger().info("[G.O.A.T. Industries] Required plugin is installed and enabled: EMCTech");
        }

        Plugin slimefun = plugin.getServer().getPluginManager().getPlugin("Slimefun");
        if (slimefun == null || !slimefun.isEnabled()) {
            plugin.getLogger().severe("[G.O.A.T. Industries] Slimefun is missing or disabled; item checks cannot run.");
            return false;
        }

        Set<String> enabledItemIds;
        try {
            enabledItemIds = readEnabledSlimefunItemIds(slimefun);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().log(Level.SEVERE,
                "[G.O.A.T. Industries] Could not read Slimefun's enabled-item registry. "
                    + "Failing closed rather than assuming dependencies are present.",
                exception);
            return false;
        }

        int missingCount = 0;
        for (DependencyCatalog.ItemRequirement requirement : DependencyCatalog.items()) {
            if (!enabledItemIds.contains(requirement.id())) {
                missingCount++;
                plugin.getLogger().severe("[G.O.A.T. Industries] Missing Slimefun item"
                    + " | addon=" + requirement.addon()
                    + " | item=" + requirement.displayName()
                    + " | id=" + requirement.id());
                passed = false;
            }
        }

        if (missingCount == 0) {
            plugin.getLogger().info("[G.O.A.T. Industries] All "
                + DependencyCatalog.items().size() + " configured Slimefun item IDs are registered and enabled.");
        } else {
            plugin.getLogger().severe("[G.O.A.T. Industries] " + missingCount + " of "
                + DependencyCatalog.items().size() + " configured Slimefun item IDs are missing or disabled.");
        }

        return passed;
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
