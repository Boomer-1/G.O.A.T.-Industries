package com.boomer1.goatindustries;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class GoatTrophyCraftListener implements Listener {

    private static final String MULTIBLOCK_INTERACT_EVENT =
        "io.github.thebusybiscuit.slimefun4.api.events.MultiBlockInteractEvent";
    private static final String MULTIBLOCK_CRAFT_EVENT =
        "io.github.thebusybiscuit.slimefun4.api.events.MultiBlockCraftEvent";
    private static final String ENHANCED_CRAFTING_TABLE_ID = "ENHANCED_CRAFTING_TABLE";
    private static final String TROPHY_ID = "GOAT_TROPHY";

    private final JavaPlugin plugin;
    private final ClassLoader slimefunClassLoader;
    private final DiscordTrophyNotifier notifier;
    private final Map<UUID, PendingInteraction> pendingInteractions = new HashMap<>();

    public GoatTrophyCraftListener(JavaPlugin plugin, ClassLoader slimefunClassLoader,
                                   DiscordTrophyNotifier notifier) {
        this.plugin = plugin;
        this.slimefunClassLoader = slimefunClassLoader;
        this.notifier = notifier;
    }

    public void register() throws ReflectiveOperationException {
        Class<?> interactClass = Class.forName(MULTIBLOCK_INTERACT_EVENT, true, slimefunClassLoader);
        Class<?> craftClass = Class.forName(MULTIBLOCK_CRAFT_EVENT, true, slimefunClassLoader);

        registerEvent(interactClass, this::onMultiBlockInteract);
        registerEvent(craftClass, this::onMultiBlockCraft);
    }

    private void registerEvent(Class<?> eventClass, EventExecutor executor) {
        plugin.getServer().getPluginManager().registerEvent(
            eventClass.asSubclass(Event.class),
            this,
            EventPriority.MONITOR,
            executor,
            plugin,
            true
        );
    }

    private void onMultiBlockInteract(Listener listener, Event event) {
        try {
            Object multiBlock = invoke(event, "getMultiBlock");
            Object machine = invoke(multiBlock, "getSlimefunItem");
            if (!ENHANCED_CRAFTING_TABLE_ID.equals(invoke(machine, "getId"))) {
                return;
            }

            Object player = invoke(event, "getPlayer");
            UUID playerId = (UUID) invoke(player, "getUniqueId");
            String playerName = (String) invoke(player, "getName");
            Block clickedBlock = (Block) invoke(event, "getClickedBlock");
            Block dispenserBlock = clickedBlock.getRelative(BlockFace.DOWN);

            if (!(dispenserBlock.getState() instanceof Dispenser dispenser)) {
                return;
            }

            PendingInteraction pending = new PendingInteraction(
                dispenserBlock,
                cloneContents(dispenser.getInventory()),
                null,
                playerName
            );
            pendingInteractions.put(playerId, pending);

            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                pendingInteractions.remove(playerId, pending), 2L);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().warning("[G.O.A.T. Industries] Could not inspect a Slimefun multiblock interaction: "
                + exception.getClass().getSimpleName());
        }
    }

    private void onMultiBlockCraft(Listener listener, Event event) {
        try {
            Object machine = invoke(event, "getMachine");
            if (!ENHANCED_CRAFTING_TABLE_ID.equals(invoke(machine, "getId"))) {
                return;
            }

            Object output = invoke(event, "getOutput");
            if (!TROPHY_ID.equals(getSlimefunItemId(output))) {
                return;
            }

            Object player = invoke(event, "getPlayer");
            UUID playerId = (UUID) invoke(player, "getUniqueId");
            PendingInteraction pending = pendingInteractions.remove(playerId);
            if (pending == null) {
                return;
            }

            ItemStack[] recipeInput = (ItemStack[]) invoke(event, "getInput");
            PendingInteraction confirmedAttempt = new PendingInteraction(
                pending.dispenserBlock(),
                pending.before(),
                cloneContents(recipeInput),
                pending.playerName()
            );

            UUID capturedPlayerId = playerId;
            plugin.getServer().getScheduler().runTask(plugin, () ->
                verifyCompletedCraft(capturedPlayerId, confirmedAttempt));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().warning("[G.O.A.T. Industries] Could not inspect a G.O.A.T. Trophy craft event: "
                + exception.getClass().getSimpleName());
        }
    }

    private String getSlimefunItemId(Object itemStack) throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(
            "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem",
            true,
            slimefunClassLoader
        );
        Method getByItem = slimefunItemClass.getMethod("getByItem", ItemStack.class);
        Object slimefunItem = getByItem.invoke(null, itemStack);
        return slimefunItem == null ? null : (String) invoke(slimefunItem, "getId");
    }

    private void verifyCompletedCraft(UUID playerId, PendingInteraction pending) {
        if (!plugin.isEnabled()) {
            return;
        }

        if (!(pending.dispenserBlock().getState() instanceof Dispenser dispenser)) {
            return;
        }

        if (recipeInputsWereConsumed(pending, dispenser.getInventory())) {
            notifier.notifyTrophyCrafted(pending.playerName(), playerId);
        }
    }

    private ItemStack[] cloneContents(Inventory inventory) {
        return cloneContents(inventory.getContents());
    }

    private ItemStack[] cloneContents(ItemStack[] contents) {
        ItemStack[] copy = new ItemStack[contents.length];
        for (int slot = 0; slot < contents.length; slot++) {
            copy[slot] = contents[slot] == null ? null : contents[slot].clone();
        }
        return copy;
    }

    private boolean recipeInputsWereConsumed(PendingInteraction pending, Inventory afterInventory) {
        ItemStack[] before = pending.before();
        ItemStack[] expected = pending.expectedInput();
        ItemStack[] after = afterInventory.getContents();

        if (expected == null || before.length != after.length) {
            return false;
        }

        for (int slot = 0; slot < expected.length; slot++) {
            if (expected[slot] == null) {
                continue;
            }

            ItemStack previous = before[slot];
            ItemStack current = after[slot];
            if (previous == null || (current != null
                && previous.getAmount() == current.getAmount()
                && previous.isSimilar(current))) {
                return false;
            }
        }
        return true;
    }

    private Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private record PendingInteraction(Block dispenserBlock, ItemStack[] before,
                                      ItemStack[] expectedInput, String playerName) {
    }
}
