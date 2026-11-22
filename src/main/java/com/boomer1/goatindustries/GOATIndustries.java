package com.boomer1.goatindustries;

import java.util.logging.Level;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class GOATIndustries extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("[G.O.A.T. Industries] Scheduling startup dependency checks for the next server tick.");

        getServer().getScheduler().runTask(this, () -> {
            if (!isEnabled()) {
                return;
            }

            getLogger().info("[G.O.A.T. Industries] Running startup dependency checks.");

            if (!DependencyVerifier.verify(this)) {
                getLogger().severe("[G.O.A.T. Industries] Dependency checks failed. Disabling G.O.A.T. Industries.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            Plugin slimefun = getServer().getPluginManager().getPlugin("Slimefun");
            try {
                SlimefunContentRegistrar.register(this, slimefun);
                getLogger().info("[G.O.A.T. Industries] G.O.A.T. Trophy content loaded successfully.");
            } catch (ReflectiveOperationException | RuntimeException exception) {
                getLogger().log(Level.SEVERE,
                    "[G.O.A.T. Industries] Could not register the G.O.A.T. Trophy with Slimefun. "
                        + "Disabling G.O.A.T. Industries.", exception);
                getServer().getPluginManager().disablePlugin(this);
            }
        });
    }
}
