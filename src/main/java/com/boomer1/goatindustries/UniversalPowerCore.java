package com.boomer1.goatindustries;

import java.util.List;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class UniversalPowerCore {

    public static final String ITEM_ID = "GOAT_UNIVERSAL_POWER_CORE";
    public static final String ITEM_NAME = "Universal Power Core";
    public static final String ITEM_LORE =
        "Infinite power with Infinite storage corrupts Infinitely.";

    private static final List<String> RECIPE_IDS = List.of(
        "IE_INFINITY_CAPACITOR",
        "QS_PRO_FADIANJI_FDL",
        "SUPREME_SUPREME_CAPACITOR",
        "SUPREME_SUPREME_GENERATOR",
        "LAMBDA_ELECTRICITY_STORAGE",
        "FN_POWER_XPANSION_POWER_R12",
        "DK2_PACK_9",
        "NTW_QUANTUM_STORAGE_14",
        "IE_INFINITY_CAPACITOR"
    );

    private UniversalPowerCore() {
    }

    public static void register(JavaPlugin plugin, ItemGroup itemGroup, SlimefunAddon addon) {
        SlimefunItemStack coreStack = new SlimefunItemStack(
            ITEM_ID,
            Material.OCHRE_FROGLIGHT,
            ChatColor.GOLD + ITEM_NAME,
            ChatColor.GRAY + ITEM_LORE
        );

        ItemStack[] recipe = resolveRecipe();
        new UnplaceableBlock(
            itemGroup,
            coreStack,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            recipe
        ).register(addon);
    }

    private static ItemStack[] resolveRecipe() {
        ItemStack[] ingredients = new ItemStack[RECIPE_IDS.size()];
        for (int slot = 0; slot < RECIPE_IDS.size(); slot++) {
            String id = RECIPE_IDS.get(slot);
            SlimefunItem item = SlimefunItem.getById(id);
            if (item == null) {
                throw new IllegalStateException(
                    "Required Universal Power Core ingredient is not registered: " + id
                );
            }
            ingredients[slot] = item.getItem().clone();
        }
        return ingredients;
    }
}
