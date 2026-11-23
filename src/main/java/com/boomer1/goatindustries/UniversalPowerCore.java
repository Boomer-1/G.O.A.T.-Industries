package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;

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
    private static final String NOT_PLACEABLE_CLASS =
        "io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable";
    private static final String ADDON_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.SlimefunAddon";

    private UniversalPowerCore() {
    }

    public static void register(JavaPlugin plugin, ClassLoader slimefunLoader, Object itemGroup, Object addon)
        throws ReflectiveOperationException {

        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);

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

        Class<?> coreClass = createNotPlaceableClass(plugin.getClass().getClassLoader(), slimefunLoader);
        Constructor<?> coreConstructor = coreClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object core = coreConstructor.newInstance(itemGroup, coreStack, enhancedCraftingTable, recipe);

        Method register = slimefunItemClass.getMethod("register", Class.forName(ADDON_CLASS, true, slimefunLoader));
        register.invoke(core, addon);

        plugin.getLogger().info("[G.O.A.T. Industries] Universal Power Core registered with its exact 3x3 recipe.");
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

    private static Class<?> createNotPlaceableClass(ClassLoader pluginLoader, ClassLoader slimefunLoader)
        throws ReflectiveOperationException {

        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Class<?> notPlaceableClass = Class.forName(NOT_PLACEABLE_CLASS, true, slimefunLoader);

        Constructor<?> superConstructor = slimefunItemClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );

        return new ByteBuddy()
            .subclass(slimefunItemClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
            .implement(notPlaceableClass)
            .name("com.boomer1.goatindustries.generated.NotPlaceableUniversalPowerCore")
            .defineConstructor(Visibility.PUBLIC)
            .withParameters(itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class)
            .intercept(MethodCall.invoke(superConstructor).withAllArguments())
            .make()
            .load(pluginLoader, ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
    }
}
