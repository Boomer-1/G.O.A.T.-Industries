package com.boomer1.goatindustries;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicCrystalPlacementListener implements Listener {

    private final NamespacedKey crystalMarker;

    public DynamicCrystalPlacementListener(JavaPlugin plugin) {
        this.crystalMarker = new NamespacedKey(plugin, "dynamic_crystal");
    }

    @EventHandler
    public void onDynamicCrystalPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(crystalMarker, PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }
}
