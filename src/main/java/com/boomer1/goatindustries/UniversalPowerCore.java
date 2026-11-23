package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

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

    private static final String ITEM_GROUP_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.ItemGroup";
    private static final String ITEM_STACK_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack";
    private static final String SLIMEFUN_ITEM_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem";
    private static final String RECIPE_TYPE_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType";
    private static final String UNPLACEABLE_BLOCK_CLASS =
        "io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock";
    private static final String ADDON_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.SlimefunAddon";

    private UniversalPowerCore() {
    }

    public static void register(JavaPlugin plugin, ClassLoader slimefunLoader, Object itemGroup, Object addon)
        throws ReflectiveOperationException {

        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Class<?> unplaceableBlockClass = Class.forName(UNPLACEABLE_BLOCK_CLASS, true, slimefunLoader);

        Constructor<?> stackConstructor = itemStackClass.getConstructor(
            String.class, Material.class, String.class, String[].class
        );
        ItemStack coreStack = (ItemStack) stackConstructor.newInstance(
            ITEM_ID,
            Material.OCHRE_FROGLIGHT,
            ChatColor.GOLD + ITEM_NAME,
            new String[] {ChatColor.GRAY + ITEM_LORE}
        );

        ItemStack[] recipe = resolveRecipe(slimefunLoader);
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);

        Constructor<?> coreConstructor = unplaceableBlockClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object core = coreConstructor.newInstance(itemGroup, coreStack, enhancedCraftingTable, recipe);

        Class<?> addonClass = Class.forName(ADDON_CLASS, true, slimefunLoader);
        Method register = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader)
            .getMethod("register", addonClass);
        register.invoke(core, addon);

    }

    private static ItemStack[] resolveRecipe(ClassLoader slimefunLoader) throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Method getById = slimefunItemClass.getMethod("getById", String.class);
        Method getItem = slimefunItemClass.getMethod("getItem");

        ItemStack[] ingredients = new ItemStack[RECIPE_IDS.size()];
        for (int slot = 0; slot < RECIPE_IDS.size(); slot++) {
            String id = RECIPE_IDS.get(slot);
            Object item = getById.invoke(null, id);
            if (item == null) {
                throw new IllegalStateException("Required Universal Power Core ingredient is not registered: " + id);
            }
            ingredients[slot] = ((ItemStack) getItem.invoke(item)).clone();
        }
        return ingredients;
    }
}
