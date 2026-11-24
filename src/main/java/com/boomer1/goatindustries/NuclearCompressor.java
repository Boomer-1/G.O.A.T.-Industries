package com.boomer1.goatindustries;

import java.util.List;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactivity;
import io.github.thebusybiscuit.slimefun4.implementation.items.RadioactiveItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class NuclearCompressor {

    public static final String MACHINE_ID = "GOAT_NUCLEAR_COMPRESSOR";
    public static final String MACHINE_NAME = "Nuclear Compressor";
    public static final String OUTPUT_ID = "GOAT_NUCLEAR_UU_MATTER";
    public static final String OUTPUT_NAME = "Nuclear UU Matter";

    private static final String UU_MATTER_ID = "UU_MATTER";
    private static final String PROGRESS_KEY = "nuclear_uu_matter_progress";
    private static final int ENERGY_CAPACITY = 16_000_000;
    private static final int ENERGY_PER_SLIMEFUN_TICK = 500_000;
    private static final int UU_MATTER_PER_TICK = 64;
    private static final int UU_MATTER_REQUIRED = 1_000_000;
    private static final int[] INPUT_SLOTS = {19, 20};
    private static final int[] OUTPUT_SLOTS = {24, 25};

    private NuclearCompressor() {
    }

    public static void register(
        JavaPlugin plugin,
                ItemGroup machineGroup,
        ItemGroup resourceGroup,
        SlimefunAddon addon
    ) {
        SlimefunItemStack machineStack = new SlimefunItemStack(
            MACHINE_ID,
            Material.BLAST_FURNACE,
            ChatColor.GOLD + MACHINE_NAME,
            ChatColor.GRAY + "Lite Expansion has nothing on this.",
            "",
            ChatColor.GRAY + "Energy Consumption: 1,000,000 J/s",
            ChatColor.GRAY + "Energy Buffer: 16,000,000 J",
            ChatColor.DARK_RED + "Breaking this machine will cause all stored UU matter to be lost"
        );

        SlimefunItemStack nuclearUuMatterStack = new SlimefunItemStack(
            OUTPUT_ID,
            Material.HEART_OF_THE_SEA,
            ChatColor.GOLD + OUTPUT_NAME,
            ChatColor.GRAY + "Extremely deadly. Wear protection",
            ChatColor.RED + "☢ Radioactive"
        );

        ItemStack uuMatterForGuide = resolveSlimefunItem(UU_MATTER_ID);
        uuMatterForGuide.setAmount(1);
        ItemStack[] nuclearUuMatterGuideRecipe = new ItemStack[9];
        nuclearUuMatterGuideRecipe[4] = uuMatterForGuide;

        RadioactiveItem nuclearUuMatter = new RadioactiveItem(
            resourceGroup,
            Radioactivity.VERY_DEADLY,
            nuclearUuMatterStack,
            new RecipeType(
                new org.bukkit.NamespacedKey(plugin, "nuclear_compressor"),
                machineStack,
                null,
                new String[] {
                    ChatColor.GREEN + "Process UU Matter in the Nuclear Compressor",
                    ChatColor.GRAY + "1,000,000 UU Matter required per output"
                }
            ),
            nuclearUuMatterGuideRecipe
        );
        nuclearUuMatter.register(addon);

        ItemStack[] craftingRecipe = resolveRecipe();
        SlimefunItem uuMatterItem = SlimefunItem.getById(UU_MATTER_ID);
        if (uuMatterItem == null) {
            throw new IllegalStateException(
                "Required Nuclear Compressor recipe ingredient is not registered: " + UU_MATTER_ID
            );
        }

        NuclearCompressorMachine machine = new NuclearCompressorMachine(
            machineGroup,
            machineStack,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            craftingRecipe,
            uuMatterItem,
            nuclearUuMatterStack
        );
        machine.setCapacity(ENERGY_CAPACITY);
        machine.setEnergyConsumption(ENERGY_PER_SLIMEFUN_TICK);
        machine.setProcessingSpeed(1);
        machine.register(addon);
    }

    private static ItemStack[] resolveRecipe() {
        return new ItemStack[] {
            resolveSlimefunItem("NUCLEAR_REACTOR"),
            resolveSlimefunItem("PLUTONIUM"),
            resolveSlimefunItem("NUCLEAR_REACTOR"),
            resolveSlimefunItem("PLUTONIUM"),
            resolveSlimefunItem("IE_INFINITY_CAPACITOR"),
            resolveSlimefunItem("PLUTONIUM"),
            resolveSlimefunItem("NUCLEAR_REACTOR"),
            resolveSlimefunItem("PLUTONIUM"),
            resolveSlimefunItem("NUCLEAR_REACTOR")
        };
    }

    private static ItemStack resolveSlimefunItem(String id) {
        SlimefunItem item = SlimefunItem.getById(id);
        if (item == null) {
            throw new IllegalStateException(
                "Required Nuclear Compressor recipe ingredient is not registered: " + id
            );
        }
        return item.getItem().clone();
    }

    private static ItemStack createProgressItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "UU Matter Processing");
        item.setItemMeta(meta);
        return item;
    }

    private static final class NuclearCompressorMachine extends AContainer {

        private static final ItemStack PROGRESS_BAR = createProgressItem();

        private final SlimefunItem uuMatterItem;
        private final ItemStack output;

        private NuclearCompressorMachine(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            SlimefunItem uuMatterItem,
            ItemStack output
        ) {
            super(itemGroup, item, recipeType, recipe);
            this.uuMatterItem = uuMatterItem;
            this.output = output.clone();
            this.output.setAmount(1);
        }

        @Override
        public ItemStack getProgressBar() {
            return PROGRESS_BAR.clone();
        }

        @Override
        public String getMachineIdentifier() {
            return MACHINE_ID;
        }

        // The machine intentionally does not refund its input when broken.
        @Override
        public int[] getInputSlots() {
            return INPUT_SLOTS.clone();
        }

        @Override
        public int[] getOutputSlots() {
            return OUTPUT_SLOTS.clone();
        }

        @Override
        protected void tick(Block block) {
            BlockMenu menu = BlockStorage.getInventory(block);
            if (menu == null) {
                return;
            }

            Location location = block.getLocation();
            String storedText = BlockStorage.getLocationInfo(location, PROGRESS_KEY);
            int stored = storedText == null ? 0 : Integer.parseInt(storedText);

            if (stored >= UU_MATTER_REQUIRED) {
                if (canFitOutput(menu)) {
                    menu.pushItem(output.clone(), OUTPUT_SLOTS);
                    BlockStorage.addBlockInfo(location, PROGRESS_KEY, "0");
                    updateProgress(menu, 0);
                } else {
                    updateProgress(menu, stored);
                }
                return;
            }

            int available = 0;
            ItemStack[] inputStacks = new ItemStack[INPUT_SLOTS.length];
            for (int i = 0; i < INPUT_SLOTS.length; i++) {
                ItemStack stack = menu.getItemInSlot(INPUT_SLOTS[i]);
                inputStacks[i] = stack;
                if (isUuMatter(stack)) {
                    available += stack.getAmount();
                }
            }

            if (available <= 0) {
                updateProgress(menu, stored);
                return;
            }

            int energyCost = getEnergyConsumption();
            int charge = getCharge(location);
            if (charge < energyCost) {
                updateProgress(menu, stored);
                return;
            }

            int amountToProcess = Math.min(
                UU_MATTER_PER_TICK,
                Math.min(available, UU_MATTER_REQUIRED - stored)
            );
            int remaining = amountToProcess;
            for (int i = 0; i < INPUT_SLOTS.length && remaining > 0; i++) {
                ItemStack stack = inputStacks[i];
                if (!isUuMatter(stack)) {
                    continue;
                }

                int consumed = Math.min(remaining, stack.getAmount());
                menu.consumeItem(INPUT_SLOTS[i], consumed);
                remaining -= consumed;
            }

            setCharge(location, charge - energyCost);
            stored += amountToProcess;
            BlockStorage.addBlockInfo(location, PROGRESS_KEY, Integer.toString(stored));

            if (stored >= UU_MATTER_REQUIRED && canFitOutput(menu)) {
                menu.pushItem(output.clone(), OUTPUT_SLOTS);
                stored = 0;
                BlockStorage.addBlockInfo(location, PROGRESS_KEY, "0");
            }

            updateProgress(menu, stored);
        }

        private boolean isUuMatter(ItemStack stack) {
            if (stack == null || stack.getType() == Material.AIR) {
                return false;
            }

            SlimefunItem item = SlimefunItem.getByItem(stack);
            return item != null && item.getId().equals(uuMatterItem.getId());
        }

        private boolean canFitOutput(BlockMenu menu) {
            for (int slot : OUTPUT_SLOTS) {
                ItemStack existing = menu.getItemInSlot(slot);
                if (existing == null || existing.getType() == Material.AIR) {
                    return true;
                }
                if (existing.isSimilar(output)
                    && existing.getAmount() < existing.getMaxStackSize()) {
                    return true;
                }
            }
            return false;
        }

        private void updateProgress(BlockMenu menu, int stored) {
            ItemStack status = new ItemStack(Material.CLOCK);
            ItemMeta meta = status.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "UU Matter Processing");
            meta.setLore(List.of(
                ChatColor.GRAY + String.format("%,d / %,d UU Matter", stored, UU_MATTER_REQUIRED),
                stored >= UU_MATTER_REQUIRED
                    ? ChatColor.GREEN + "Ready - waiting for output space"
                    : ChatColor.GRAY + "Stored UU Matter is lost if this machine is broken"
            ));
            status.setItemMeta(meta);
            menu.replaceExistingItem(22, status);
        }
    }
}
