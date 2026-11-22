package com.boomer1.goatindustries;

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

            getLogger().info("[G.O.A.T. Industries] All currently configured dependency checks passed.");
            getLogger().info("[G.O.A.T. Industries] This is a dependency-test build; gameplay content is not loaded.");
        });
    }
}
