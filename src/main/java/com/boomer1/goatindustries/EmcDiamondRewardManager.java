package com.boomer1.goatindustries;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class EmcDiamondRewardManager implements Listener {

    private static final double REWARD_THRESHOLD = 1_000_000_000D;
    private static final long CHECK_INTERVAL_TICKS = 200L;
    private static final String EMC_STORAGE_CLASS = "io.github.sefiraat.emctech.emc.EmcStorage";
    private static final String ACHIEVED_FILE_NAME = "emcachieved.yml";
    private static final String REWARD_DATA_PATH = "claimed.";

    private final JavaPlugin plugin;
    private final ItemStack emcDiamond;
    private final Method getEmcMethod;
    private final File achievedFile;
    private final YamlConfiguration achievedData;
    private final Set<UUID> rewardedThisSession = new HashSet<>();

    private boolean apiFailureLogged;

    public EmcDiamondRewardManager(JavaPlugin plugin, Plugin emcTech, ItemStack emcDiamond)
        throws ReflectiveOperationException, IOException {
        this.plugin = plugin;
        this.emcDiamond = emcDiamond.clone();

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create the G.O.A.T. Industries plugin folder.");
        }

        this.achievedFile = new File(dataFolder, ACHIEVED_FILE_NAME);
        if (!achievedFile.exists() && !achievedFile.createNewFile()) {
            throw new IOException("Could not create " + ACHIEVED_FILE_NAME + ".");
        }
        this.achievedData = YamlConfiguration.loadConfiguration(achievedFile);

        Class<?> emcStorageClass = Class.forName(EMC_STORAGE_CLASS, true, emcTech.getClass().getClassLoader());
        this.getEmcMethod = emcStorageClass.getMethod("getEmc", UUID.class);
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkOnlinePlayers, 1L, CHECK_INTERVAL_TICKS);
        plugin.getLogger().info("[G.O.A.T. Industries] EMC Diamond reward monitoring started "
            + "(checking every 10 seconds).");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> checkPlayer(event.getPlayer()));
    }

    private void checkOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayer(player);
        }
    }

    private void checkPlayer(Player player) {
        UUID playerId = player.getUniqueId();

        if (hasReceivedReward(playerId)) {
            return;
        }

        final double currentEmc;
        try {
            Object value = getEmcMethod.invoke(null, playerId);
            if (!(value instanceof Number number)) {
                throw new IllegalStateException("EMCTech returned a non-numeric EMC balance.");
            }
            currentEmc = number.doubleValue();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            if (!apiFailureLogged) {
                apiFailureLogged = true;
                plugin.getLogger().log(Level.SEVERE,
                    "[G.O.A.T. Industries] Could not read player EMC from EMCTech. "
                        + "EMC Diamond rewards are paused.", exception);
            }
            return;
        }

        if (currentEmc < REWARD_THRESHOLD) {
            return;
        }

        rewardPlayer(player);
    }

    private boolean hasReceivedReward(UUID playerId) {
        return rewardedThisSession.contains(playerId)
            || achievedData.getBoolean(REWARD_DATA_PATH + playerId, false);
    }

    private void rewardPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        if (!rewardedThisSession.add(playerId)) {
            return;
        }

        achievedData.set(REWARD_DATA_PATH + playerId, true);
        try {
            achievedData.save(achievedFile);
        } catch (IOException exception) {
            rewardedThisSession.remove(playerId);
            plugin.getLogger().log(Level.SEVERE,
                "[G.O.A.T. Industries] Could not save the EMC Diamond achievement for "
                    + player.getName() + ". The reward was not issued.", exception);
            return;
        }

        PlayerInventory inventory = player.getInventory();
        int emptySlot = inventory.firstEmpty();

        if (emptySlot >= 0) {
            inventory.setItem(emptySlot, emcDiamond.clone());
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), emcDiamond.clone());
        }

        player.sendMessage("Congratulations on achieving 1 Billion EMC!");
        plugin.getLogger().info("[G.O.A.T. Industries] Awarded an EMC Diamond to " + player.getName() + ".");
    }
}
