package com.boomer1.goatindustries;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
    private final NamespacedKey interactionKey;

    public GoatTrophyListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "goat_trophy");
        this.displayKey = new NamespacedKey(plugin, "placed_goat_trophy");
        this.interactionKey = new NamespacedKey(plugin, "goat_trophy_interaction");
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
        Interaction hitbox = null;
        try {
            display = clickedBlock.getWorld().spawn(displayLocation, ItemDisplay.class);
            display.setItemStack(heldItem.clone());
            display.setItemDisplayTransform(ItemDisplayTransform.GROUND);
            display.setCustomName(ChatColor.GOLD + "G.O.A.T.");
            display.setCustomNameVisible(true);
            display.setPersistent(true);
            display.setInvulnerable(true);
            display.setSilent(true);
            display.getPersistentDataContainer().set(displayKey, PersistentDataType.BYTE, (byte) 1);

            // ItemDisplay entities have no usable player hitbox. This invisible entity
            // supplies a reliable click/attack target for removing the visible trophy.
            hitbox = clickedBlock.getWorld().spawn(displayLocation, Interaction.class);
            hitbox.setInteractionWidth(1.25F);
            hitbox.setInteractionHeight(1.5F);
            hitbox.setResponsive(true);
            hitbox.setPersistent(true);
            hitbox.getPersistentDataContainer().set(interactionKey, PersistentDataType.BYTE, (byte) 1);

            consumeOne(heldItem, event.getPlayer().getInventory(), event.getHand());
            event.getPlayer().sendMessage(ChatColor.GOLD
                + "The G.O.A.T. Trophy has been placed. Congratulations!");
        } catch (RuntimeException exception) {
            if (hitbox != null && hitbox.isValid()) {
                hitbox.remove();
            }
            if (display != null && display.isValid()) {
                display.remove();
            }
            plugin.getLogger().severe("Could not place the G.O.A.T. Trophy: " + exception.getMessage());
            event.getPlayer().sendMessage(ChatColor.RED
                + "The G.O.A.T. Trophy could not be placed. Your item has not been consumed.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTrophyClicked(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction hitbox) || !isTrophyHitbox(hitbox)) {
            return;
        }

        event.setCancelled(true);
        recoverTrophy(hitbox, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTrophyDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction hitbox)
            || !isTrophyHitbox(hitbox)
            || !(event.getDamager() instanceof Player player)) {
            return;
        }

        event.setCancelled(true);
        recoverTrophy(hitbox, player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location placedLocation = event.getBlockPlaced().getLocation();
        if (!hasTrophyAtBlock(placedLocation)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED
            + "You cannot place a block inside the space occupied by the G.O.A.T. Trophy.");
    }

    private void recoverTrophy(Interaction hitbox, Player player) {
        ItemDisplay display = findTrophyDisplay(hitbox.getLocation());
        ItemStack trophy = display == null ? createTrophyItem() : display.getItemStack().clone();
        if (!isTrophy(trophy)) {
            trophy = createTrophyItem();
        }

        if (display != null && display.isValid()) {
            display.remove();
        }
        hitbox.remove();

        giveTrophy(player, trophy);
        player.sendMessage(ChatColor.GOLD + "You recovered the G.O.A.T. Trophy.");
    }

    private boolean isTrophy(ItemStack item) {
        if (item == null || item.getType() != Material.GOAT_SPAWN_EGG || !item.hasItemMeta()) {
            return false;
        }

        Byte marker = item.getItemMeta().getPersistentDataContainer()
            .get(itemKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    private boolean isPlacedTrophy(ItemDisplay display) {
        Byte marker = display.getPersistentDataContainer().get(displayKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    private boolean isTrophyHitbox(Interaction hitbox) {
        Byte marker = hitbox.getPersistentDataContainer().get(interactionKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    private ItemDisplay findTrophyDisplay(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return null;
        }

        for (Entity entity : world.getNearbyEntities(location, 2.0, 2.0, 2.0)) {
            if (entity instanceof ItemDisplay display && isPlacedTrophy(display)) {
                return display;
            }
        }
        return null;
    }

    private boolean hasTrophyAt(Location location) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 1.0, 1.0, 1.0)) {
            if (entity instanceof ItemDisplay display && isPlacedTrophy(display)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTrophyAtBlock(Location blockLocation) {
        for (Entity entity : blockLocation.getWorld().getNearbyEntities(
            blockLocation.clone().add(0.5, 0.5, 0.5), 0.75, 0.75, 0.75)) {
            if (entity instanceof ItemDisplay display
                && isPlacedTrophy(display)
                && display.getLocation().getBlockX() == blockLocation.getBlockX()
                && display.getLocation().getBlockY() == blockLocation.getBlockY()
                && display.getLocation().getBlockZ() == blockLocation.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    private ItemStack createTrophyItem() {
        ItemStack trophy = new ItemStack(Material.GOAT_SPAWN_EGG);
        ItemMeta meta = trophy.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "G.O.A.T. Trophy");
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        trophy.setItemMeta(meta);
        return trophy;
    }

    private void giveTrophy(Player player, ItemStack trophy) {
        if (trophy == null || trophy.getType() != Material.GOAT_SPAWN_EGG) {
            trophy = createTrophyItem();
        }

        if (player.getInventory().addItem(trophy).isEmpty()) {
            return;
        }

        player.getWorld().dropItemNaturally(player.getLocation(), trophy);
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
