package com.boomer1.goatindustries;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class GoatTrophyListener implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey displayKey;

    public GoatTrophyListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "goat_trophy");
        this.displayKey = new NamespacedKey(plugin, "placed_goat_trophy");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTrophyInteract(PlayerInteractEvent event) {
        ItemStack heldItem = getHeldItem(event.getPlayer().getInventory(), event.getHand());
        if (!isTrophy(heldItem)) {
            return;
        }

        // A trophy egg must never perform vanilla spawn-egg behavior.
        event.setCancelled(true);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (!PyramidStructure.isValidPyramid(clickedBlock)) {
            event.getPlayer().sendMessage(ChatColor.RED
                + "The G.O.A.T. Trophy requires a complete five-tier Netherite pyramid.");
            event.getPlayer().sendMessage(ChatColor.GRAY
                + "Build layers of 9x9, 7x7, 5x5, 3x3, and 1x1 blocks, centered on one another.");
            return;
        }

        Location displayLocation = clickedBlock.getLocation().add(0.5, 1.15, 0.5);
        if (hasTrophyAt(displayLocation)) {
            event.getPlayer().sendMessage(ChatColor.YELLOW
                + "A G.O.A.T. Trophy is already placed on this pyramid.");
            return;
        }

        ItemDisplay display = null;
        try {
            display = clickedBlock.getWorld().spawn(displayLocation, ItemDisplay.class);
            ItemStack displayItem = new ItemStack(Material.GOAT_SPAWN_EGG);
            ItemMeta meta = displayItem.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "G.O.A.T.");
            displayItem.setItemMeta(meta);

            display.setItemStack(displayItem);
            display.setItemDisplayTransform(ItemDisplayTransform.GROUND);
            display.setCustomName(ChatColor.GOLD + "G.O.A.T.");
            display.setCustomNameVisible(true);
            display.setPersistent(true);
            display.setInvulnerable(true);
            display.setSilent(true);
            display.getPersistentDataContainer().set(displayKey, PersistentDataType.BYTE, (byte) 1);

            consumeOne(heldItem, event.getPlayer().getInventory(), event.getHand());
            event.getPlayer().sendMessage(ChatColor.GOLD
                + "The G.O.A.T. Trophy has been placed. Congratulations!");
        } catch (RuntimeException exception) {
            if (display != null && display.isValid()) {
                display.remove();
            }
            plugin.getLogger().severe("Could not place the G.O.A.T. Trophy: " + exception.getMessage());
            event.getPlayer().sendMessage(ChatColor.RED
                + "The G.O.A.T. Trophy could not be placed. Your item has not been consumed.");
        }
    }

    private boolean isTrophy(ItemStack item) {
        if (item == null || item.getType() != Material.GOAT_SPAWN_EGG || !item.hasItemMeta()) {
            return false;
        }

        Byte marker = item.getItemMeta().getPersistentDataContainer()
            .get(itemKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    private boolean hasTrophyAt(Location location) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 1.0, 1.0, 1.0)) {
            if (entity instanceof ItemDisplay display
                && display.getPersistentDataContainer().has(displayKey, PersistentDataType.BYTE)) {
                return true;
            }
        }
        return false;
    }

    private ItemStack getHeldItem(org.bukkit.inventory.PlayerInventory inventory, EquipmentSlot hand) {
        return hand == EquipmentSlot.OFF_HAND ? inventory.getItemInOffHand() : inventory.getItemInMainHand();
    }

    private void consumeOne(ItemStack heldItem, org.bukkit.inventory.PlayerInventory inventory, EquipmentSlot hand) {
        if (heldItem.getAmount() <= 1) {
            if (hand == EquipmentSlot.OFF_HAND) {
                inventory.setItemInOffHand(null);
            } else {
                inventory.setItemInMainHand(null);
            }
        } else {
            heldItem.setAmount(heldItem.getAmount() - 1);
        }
    }
}
