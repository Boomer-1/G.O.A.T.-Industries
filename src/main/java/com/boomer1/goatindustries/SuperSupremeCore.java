package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public final class SuperSupremeCore {

    public static final String ITEM_ID = "GOAT_SUPER_SUPREME_CORE";
    public static final String ITEM_NAME = "Super Supreme Core";
    public static final String TEXTURE_URL =
        "https://textures.minecraft.net/texture/759a61e113ccbedaec57cb968680cfaca0d987ff2d083f5aef0586a50316b28d";

    private static final String[] RECIPE_IDS = {
        "SUPREME_CORE_OF_BLOCK",
        "SUPREME_CORE_OF_DEATH",
        "SUPREME_CORE_OF_LIFE",
        "SUPREME_THORNIUM_ENERGIZED",
        "SUPREME_SUPREME",
        "SUPREME_THORNIUM_ENERGIZED",
        "SUPREME_CORE_OF_COLOR",
        "SUPREME_CORE_OF_NATURE",
        "SUPREME_CORE_OF_ALLOY"
    };

    private static final String MULTIBLOCK_MAGICAL_FABRICATOR_CLASS =
        "com.github.relativobr.supreme.machine.multiblock.MultiBlockMagicalFabricator";
    private static final String ABSTRACT_ITEM_RECIPE_CLASS =
        "com.github.relativobr.supreme.generic.recipe.AbstractItemRecipe";

    private static final String[] ELECTRIC_MAGICAL_MACHINE_IDS = {
        "SUPREME_ELECTRIC_MAGICAL_I",
        "SUPREME_ELECTRIC_MAGICAL_II",
        "SUPREME_ELECTRIC_MAGICAL_III"
    };

    private SuperSupremeCore() {
    }

    public static void register(JavaPlugin plugin, ItemGroup itemGroup, SlimefunAddon addon)
        throws ReflectiveOperationException {

        SlimefunItemStack outputStack = new SlimefunItemStack(
            ITEM_ID,
            Material.PLAYER_HEAD,
            ChatColor.GOLD + ITEM_NAME,
            "How is it possible to put so much in so small a space?"
        );
        setHeadTexture(outputStack);

        Plugin supreme = plugin.getServer().getPluginManager().getPlugin("Supreme");
        if (supreme == null || !supreme.isEnabled()) {
            throw new IllegalStateException("The Supreme addon is not enabled.");
        }
        ClassLoader supremeLoader = supreme.getClass().getClassLoader();

        ItemStack[] recipe = resolveRecipe();
        RecipeType multiblockRecipeType = getMultiblockMagicalRecipeType(supremeLoader);

        new UnplaceableBlock(itemGroup, outputStack, multiblockRecipeType, recipe).register(addon);

        registerElectricMagicalRecipes(supremeLoader, outputStack, recipe);
    }

    private static RecipeType getMultiblockMagicalRecipeType(ClassLoader supremeLoader)
        throws ReflectiveOperationException {
        Class<?> machineClass = Class.forName(
            MULTIBLOCK_MAGICAL_FABRICATOR_CLASS, true, supremeLoader
        );
        return (RecipeType) machineClass.getMethod("getMachine").invoke(null);
    }

    private static void registerElectricMagicalRecipes(
        ClassLoader supremeLoader,
        ItemStack outputStack,
        ItemStack[] recipe
    ) throws ReflectiveOperationException {
        Class<?> abstractRecipeClass = Class.forName(
            ABSTRACT_ITEM_RECIPE_CLASS, true, supremeLoader
        );
        Constructor<?> recipeConstructor = abstractRecipeClass.getConstructor(
            ItemStack[].class, ItemStack[].class
        );
        ItemStack[] output = {outputStack.clone()};

        for (String machineId : ELECTRIC_MAGICAL_MACHINE_IDS) {
            SlimefunItem machine = SlimefunItem.getById(machineId);
            if (machine == null) {
                throw new IllegalStateException(
                    "Required Supreme Electric Magical Machine is not registered: " + machineId
                );
            }

            Field recipesField = machine.getClass().getField("machineRecipes");
            Object recipesObject = recipesField.get(machine);
            if (!(recipesObject instanceof List<?>)) {
                throw new IllegalStateException(
                    "Supreme Electric Magical Machine has no accessible recipe list: " + machineId
                );
            }

            @SuppressWarnings("unchecked")
            List<Object> machineRecipes = (List<Object>) recipesObject;
            Object newRecipe = recipeConstructor.newInstance(recipe.clone(), output.clone());
            machineRecipes.add(newRecipe);
        }
    }

    private static ItemStack[] resolveRecipe() {
        ItemStack[] ingredients = new ItemStack[RECIPE_IDS.length];
        for (int slot = 0; slot < RECIPE_IDS.length; slot++) {
            String id = RECIPE_IDS[slot];
            SlimefunItem item = SlimefunItem.getById(id);
            if (item == null) {
                throw new IllegalStateException(
                    "Required Super Supreme Core ingredient is not registered: " + id
                );
            }

            ingredients[slot] = item.getItem().clone();
            ingredients[slot].setAmount(64);
        }
        return ingredients;
    }

    private static void setHeadTexture(ItemStack stack) {
        if (!(stack.getItemMeta() instanceof SkullMeta skullMeta)) {
            throw new IllegalStateException("The Super Supreme Core item is not a player head.");
        }

        UUID profileId = UUID.nameUUIDFromBytes(
            (ITEM_ID + ":" + ITEM_NAME).getBytes(StandardCharsets.UTF_8)
        );
        PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "GoatSuperCore");
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(TEXTURE_URL));
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("The Super Supreme Core texture URL is invalid.", exception);
        }
        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);
        stack.setItemMeta(skullMeta);
    }
}
