package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

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

    private static final String ITEM_GROUP_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.ItemGroup";
    private static final String ITEM_STACK_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack";
    private static final String SLIMEFUN_ITEM_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem";
    private static final String RECIPE_TYPE_CLASS =
        "io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType";
    private static final String A_CONTAINER_CLASS =
        "me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer";
    private static final String UNPLACEABLE_BLOCK_CLASS =
        "io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock";

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

    private ImplosivePress() {
    }

    public static void register(JavaPlugin plugin, ClassLoader slimefunLoader, Object machineGroup, Object resourceGroup, Object addon)
        throws ReflectiveOperationException {

        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Class<?> addonClass = Class.forName(
            "io.github.thebusybiscuit.slimefun4.api.SlimefunAddon", true, slimefunLoader
        );

        ItemStack machineStack = createSlimefunStack(
            slimefunLoader,
            MACHINE_ID,
            Material.WARPED_WART_BLOCK,
            ChatColor.GOLD + MACHINE_NAME,
            new String[] {
                ChatColor.GRAY + "There is absolutely no way to make anything more compact",
                "",
                ChatColor.DARK_PURPLE + "End-Game Machine",
                ChatColor.GRAY + "Energy Consumption: 1,000,000 J/s",
                ChatColor.GRAY + "Energy Buffer: 16,000,000 J"
            }
        );

        ItemStack outputStack = createSlimefunStack(
            slimefunLoader,
            OUTPUT_ID,
            Material.COBBLESTONE,
            ChatColor.GOLD + OUTPUT_NAME,
            new String[] {
                ChatColor.GRAY + "Ready to implode at any given moment"
            }
        );
        ItemMeta outputMeta = outputStack.getItemMeta();
        outputMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        outputMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        outputStack.setItemMeta(outputMeta);

        Class<?> unplaceableBlockClass = Class.forName(UNPLACEABLE_BLOCK_CLASS, true, slimefunLoader);
        Constructor<?> outputConstructor = unplaceableBlockClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Constructor<?> displayRecipeTypeConstructor = recipeTypeClass.getConstructor(
            NamespacedKey.class, ItemStack.class
        );
        Object implosivePressRecipeType = displayRecipeTypeConstructor.newInstance(
            new NamespacedKey(plugin, "implosive_press"), machineStack
        );
        ItemStack compressedCobblestoneForGuide = resolveItem(
            slimefunLoader, COMPRESSED_COBBLESTONE_ID
        );
        compressedCobblestoneForGuide.setAmount(1);
        ItemStack[] implosiveCobblestoneGuideRecipe = new ItemStack[9];
        implosiveCobblestoneGuideRecipe[4] = compressedCobblestoneForGuide;
        Object outputItem = outputConstructor.newInstance(
            resourceGroup, outputStack, implosivePressRecipeType,
            implosiveCobblestoneGuideRecipe
        );
        Method registerItem = slimefunItemClass.getMethod("register", addonClass);
        registerItem.invoke(outputItem, addon);

        ItemStack[] craftingRecipe = resolveItems(slimefunLoader, CRAFTING_INGREDIENT_IDS);
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);

        ItemStack progressBar = new ItemStack(Material.CLOCK);
        ItemMeta progressMeta = progressBar.getItemMeta();
        progressMeta.setDisplayName(ChatColor.YELLOW + "Implosive Press");
        progressBar.setItemMeta(progressMeta);

        Class<?> machineClass = createMachineClass(
            plugin.getClass().getClassLoader(), slimefunLoader, progressBar
        );
        Constructor<?> machineConstructor = machineClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object machine = machineConstructor.newInstance(
            machineGroup, machineStack, enhancedCraftingTable, craftingRecipe
        );

        machineClass.getMethod("setCapacity", int.class).invoke(machine, ENERGY_CAPACITY);
        machineClass.getMethod("setEnergyConsumption", int.class)
            .invoke(machine, ENERGY_PER_SLIMEFUN_TICK);
        machineClass.getMethod("setProcessingSpeed", int.class).invoke(machine, 1);

        Method registerMachineRecipe = machineClass.getMethod(
            "registerRecipe", int.class, ItemStack[].class, ItemStack[].class
        );
        ItemStack compressedCobblestone = resolveItem(slimefunLoader, COMPRESSED_COBBLESTONE_ID);
        compressedCobblestone.setAmount(64);
        registerMachineRecipe.invoke(
            machine,
            PROCESSING_SECONDS,
            new ItemStack[] {compressedCobblestone},
            new ItemStack[] {outputStack.clone()}
        );

        registerItem.invoke(machine, addon);

    }

    private static Class<?> createMachineClass(
        ClassLoader pluginLoader, ClassLoader slimefunLoader, ItemStack progressBar
    ) throws ReflectiveOperationException {
        Class<?> aContainerClass = Class.forName(A_CONTAINER_CLASS, true, slimefunLoader);
        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Constructor<?> superConstructor = aContainerClass.getDeclaredConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );

        return new ByteBuddy()
            .subclass(aContainerClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
            .name("com.boomer1.goatindustries.generated.ImplosivePressMachine")
            .defineConstructor(Visibility.PUBLIC)
            .withParameters(itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class)
            .intercept(MethodCall.invoke(superConstructor).withAllArguments())
            .method(ElementMatchers.named("getProgressBar").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(progressBar))
            .method(ElementMatchers.named("getMachineIdentifier").and(ElementMatchers.takesArguments(0)))
            .intercept(FixedValue.value(MACHINE_ID))
            .make()
            .load(pluginLoader, ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
    }

    private static ItemStack createSlimefunStack(
        ClassLoader loader, String id, Material material, String displayName, String[] lore
    ) throws ReflectiveOperationException {
        Class<?> stackClass = Class.forName(ITEM_STACK_CLASS, true, loader);
        Constructor<?> constructor = stackClass.getConstructor(
            String.class, Material.class, String.class, String[].class
        );
        return (ItemStack) constructor.newInstance(id, material, displayName, lore);
    }

    private static ItemStack[] resolveItems(ClassLoader loader, String[] ids)
        throws ReflectiveOperationException {
        ItemStack[] items = new ItemStack[ids.length];
        for (int i = 0; i < ids.length; i++) {
            items[i] = resolveItem(loader, ids[i]);
        }
        return items;
    }

    private static ItemStack resolveItem(ClassLoader loader, String id)
        throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, loader);
        Object item = slimefunItemClass.getMethod("getById", String.class).invoke(null, id);
        if (item == null) {
            throw new IllegalStateException("Required Implosive Press ingredient is not registered: " + id);
        }
        return ((ItemStack) slimefunItemClass.getMethod("getItem").invoke(item)).clone();
    }

}
