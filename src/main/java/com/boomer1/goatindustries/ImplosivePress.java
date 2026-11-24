package com.boomer1.goatindustries;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImplosivePress {

    public static final String MACHINE_ID = "GOAT_IMPLOSIVE_PRESS";
    public static final String MACHINE_NAME = "Implosive Press";
    public static final String OUTPUT_ID = "GOAT_IMPLOSIVE_COBBLESTONE";
    public static final String OUTPUT_NAME = "Implosive Cobblestone";

    private static final String[] CRAFTING_INGREDIENT_IDS = {
        "IE_VOID_CAPACITOR",
        "MACERATOR",
        "IE_VOID_CAPACITOR",
        "SUPREME_VENTUS_GENERATOR",
        "CARBONADO_EDGED_FURNACE",
        "SUPREME_VENTUS_GENERATOR",
        "IE_VOID_CAPACITOR",
        "MACERATOR",
        "IE_VOID_CAPACITOR"
    };

    private static final String COMPRESSED_COBBLESTONE_ID = "MOMOTECH_COBBLESTONE49";
    private static final int ENERGY_CAPACITY = 16_000_000;
    private static final int ENERGY_PER_SLIMEFUN_TICK = 500_000;
    private static final int PROCESSING_SECONDS = 30;
    private static final int[] INPUT_SLOTS = {19, 20};
    private static final int[] OUTPUT_SLOTS = {24, 25};

    private ImplosivePress() {
    }

    public static void register(
        JavaPlugin plugin,
                ItemGroup machineGroup,
        ItemGroup resourceGroup,
        SlimefunAddon addon
    ) {
        SlimefunItemStack machineStack = new SlimefunItemStack(
            MACHINE_ID,
            Material.WARPED_WART_BLOCK,
            ChatColor.GOLD + MACHINE_NAME,
            ChatColor.GRAY + "There is absolutely no way to make anything more compact",
            "",
            ChatColor.DARK_PURPLE + "End-Game Machine",
            ChatColor.GRAY + "Energy Consumption: 1,000,000 J/s",
            ChatColor.GRAY + "Energy Buffer: 16,000,000 J"
        );

        SlimefunItemStack outputStack = new SlimefunItemStack(
            OUTPUT_ID,
            Material.COBBLESTONE,
            ChatColor.GOLD + OUTPUT_NAME,
            ChatColor.GRAY + "Ready to implode at any given moment"
        );
        ItemMeta outputMeta = outputStack.getItemMeta();
        outputMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        outputMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        outputStack.setItemMeta(outputMeta);

        ItemStack compressedCobblestoneForGuide = resolveItem(COMPRESSED_COBBLESTONE_ID);
        compressedCobblestoneForGuide.setAmount(64);
        ItemStack[] implosiveCobblestoneGuideRecipe = new ItemStack[9];
        implosiveCobblestoneGuideRecipe[4] = compressedCobblestoneForGuide;

        RecipeType implosivePressRecipeType = new RecipeType(
            new NamespacedKey(plugin, "implosive_press"),
            machineStack
        );
        new UnplaceableBlock(
            resourceGroup,
            outputStack,
            implosivePressRecipeType,
            implosiveCobblestoneGuideRecipe
        ).register(addon);

        ItemStack[] craftingRecipe = resolveItems(CRAFTING_INGREDIENT_IDS);
        ImplosivePressMachine machine = new ImplosivePressMachine(
            machineGroup,
            machineStack,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            craftingRecipe
        );
        machine.setCapacity(ENERGY_CAPACITY);
        machine.setEnergyConsumption(ENERGY_PER_SLIMEFUN_TICK);
        machine.setProcessingSpeed(1);

        ItemStack compressedCobblestone = resolveItem(COMPRESSED_COBBLESTONE_ID);
        compressedCobblestone.setAmount(64);
        machine.registerRecipe(
            PROCESSING_SECONDS,
            new ItemStack[] {compressedCobblestone},
            new ItemStack[] {outputStack.clone()}
        );

        machine.register(addon);
    }

    private static ItemStack[] resolveItems(String[] ids) {
        ItemStack[] items = new ItemStack[ids.length];
        for (int i = 0; i < ids.length; i++) {
            items[i] = resolveItem(ids[i]);
        }
        return items;
    }

    private static ItemStack resolveItem(String id) {
        SlimefunItem item = SlimefunItem.getById(id);
        if (item == null) {
            throw new IllegalStateException(
                "Required Implosive Press ingredient is not registered: " + id
            );
        }
        return item.getItem().clone();
    }

    private static ItemStack createProgressItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + MACHINE_NAME);
        item.setItemMeta(meta);
        return item;
    }

    private static final class ImplosivePressMachine extends AContainer {

        private static final ItemStack PROGRESS_BAR = createProgressItem();

        private ImplosivePressMachine(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe
        ) {
            super(itemGroup, item, recipeType, recipe);
        }

        @Override
        public ItemStack getProgressBar() {
            return PROGRESS_BAR.clone();
        }

        @Override
        public String getMachineIdentifier() {
            return MACHINE_ID;
        }

        @Override
        public int[] getInputSlots() {
            return INPUT_SLOTS.clone();
        }

        @Override
        public int[] getOutputSlots() {
            return OUTPUT_SLOTS.clone();
        }
    }
}
