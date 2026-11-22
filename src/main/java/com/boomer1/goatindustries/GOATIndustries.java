package com.boomer1.goatindustries;

import java.io.IOException;
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
            Plugin emcTech = getServer().getPluginManager().getPlugin("EMCTech");

            try {
                if (slimefun == null || !slimefun.isEnabled()) {
                    throw new IllegalStateException("Slimefun is not available.");
                }
                if (emcTech == null || !emcTech.isEnabled()) {
                    throw new IllegalStateException("EMCTech is not available.");
                }

                SlimefunContentRegistrar.register(this, slimefun, emcTech);
                getLogger().info("[G.O.A.T. Industries] Content and EMC Diamond reward monitoring loaded successfully.");
            } catch (ReflectiveOperationException | IOException | RuntimeException exception) {
                getLogger().log(Level.SEVERE,
                    "[G.O.A.T. Industries] Could not register G.O.A.T. Industries content or EMC rewards. "
                        + "Disabling G.O.A.T. Industries.", exception);
                getServer().getPluginManager().disablePlugin(this);
            }
        });
    }
}
