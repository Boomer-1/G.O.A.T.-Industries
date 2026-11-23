package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
        "SUPREME_CORE_OF_LIFE"
    };

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

    public static void register(JavaPlugin plugin, ClassLoader slimefunLoader, Object itemGroup, Object addon)
        throws ReflectiveOperationException {

        Class<?> itemGroupClass = Class.forName(ITEM_GROUP_CLASS, true, slimefunLoader);
        Class<?> itemStackClass = Class.forName(ITEM_STACK_CLASS, true, slimefunLoader);
        Class<?> recipeTypeClass = Class.forName(RECIPE_TYPE_CLASS, true, slimefunLoader);
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Class<?> unplaceableBlockClass = Class.forName(UNPLACEABLE_BLOCK_CLASS, true, slimefunLoader);
        Class<?> addonClass = Class.forName(ADDON_CLASS, true, slimefunLoader);

        Constructor<?> stackConstructor = itemStackClass.getConstructor(
            String.class, Material.class, String.class, String[].class
        );
        ItemStack outputStack = (ItemStack) stackConstructor.newInstance(
            ITEM_ID,
            Material.PLAYER_HEAD,
            ChatColor.GOLD + ITEM_NAME,
            new String[0]
        );
        setHeadTexture(outputStack, slimefunLoader);

        Plugin supreme = plugin.getServer().getPluginManager().getPlugin("Supreme");
        if (supreme == null || !supreme.isEnabled()) {
            throw new IllegalStateException("The Supreme addon is not enabled.");
        }
        ClassLoader supremeLoader = supreme.getClass().getClassLoader();

        ItemStack[] recipe = resolveRecipe(slimefunLoader);
        Object multiblockRecipeType = getMultiblockMagicalRecipeType(supremeLoader);

        Constructor<?> outputConstructor = unplaceableBlockClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object outputItem = outputConstructor.newInstance(
            itemGroup, outputStack, multiblockRecipeType, recipe
        );

        Method registerItem = slimefunItemClass.getMethod("register", addonClass);
        registerItem.invoke(outputItem, addon);

        registerElectricMagicalRecipes(
            slimefunLoader, supremeLoader, slimefunItemClass, outputStack, recipe
        );

        plugin.getLogger().info(
            "[G.O.A.T. Industries] Super Supreme Core registered for the Magical Fabricator "
                + "and all three Electric Magical Machine tiers."
        );
    }

    private static Object getMultiblockMagicalRecipeType(ClassLoader slimefunLoader)
        throws ReflectiveOperationException {
        Class<?> machineClass = Class.forName(
            MULTIBLOCK_MAGICAL_FABRICATOR_CLASS, true, slimefunLoader
        );
        return machineClass.getMethod("getMachine").invoke(null);
    }

    private static void registerElectricMagicalRecipes(
        ClassLoader slimefunLoader,
        ClassLoader supremeLoader,
        Class<?> slimefunItemClass,
        ItemStack outputStack,
        ItemStack[] recipe
    ) throws ReflectiveOperationException {

        Method getById = slimefunItemClass.getMethod("getById", String.class);
        Class<?> abstractRecipeClass = Class.forName(
            ABSTRACT_ITEM_RECIPE_CLASS, true, supremeLoader
        );
        Constructor<?> recipeConstructor = abstractRecipeClass.getConstructor(
            ItemStack[].class, ItemStack[].class
        );
        ItemStack[] output = {outputStack.clone()};

        for (String machineId : ELECTRIC_MAGICAL_MACHINE_IDS) {
            Object machine = getById.invoke(null, machineId);
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

    private static ItemStack[] resolveRecipe(ClassLoader slimefunLoader)
        throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Method getById = slimefunItemClass.getMethod("getById", String.class);
        Method getItem = slimefunItemClass.getMethod("getItem");

        ItemStack[] ingredients = new ItemStack[RECIPE_IDS.length];
        for (int slot = 0; slot < RECIPE_IDS.length; slot++) {
            String id = RECIPE_IDS[slot];
            Object item = getById.invoke(null, id);
            if (item == null) {
                throw new IllegalStateException(
                    "Required Super Supreme Core ingredient is not registered: " + id
                );
            }

            ingredients[slot] = ((ItemStack) getItem.invoke(item)).clone();
            ingredients[slot].setAmount(64);
        }
        return ingredients;
    }

    private static void setHeadTexture(ItemStack stack, ClassLoader slimefunLoader)
        throws ReflectiveOperationException {
        ItemMeta meta = stack.getItemMeta();
        Class<?> skullMetaClass = Class.forName(
            "org.bukkit.inventory.meta.SkullMeta", true, slimefunLoader
        );
        if (!skullMetaClass.isInstance(meta)) {
            throw new IllegalStateException("The Super Supreme Core item is not a player head.");
        }

        Class<?> profileClass = Class.forName(
            "org.bukkit.profile.PlayerProfile", true, slimefunLoader
        );
        Class<?> texturesClass = Class.forName(
            "org.bukkit.profile.PlayerTextures", true, slimefunLoader
        );
        UUID profileId = UUID.nameUUIDFromBytes(
            (ITEM_ID + ":" + ITEM_NAME).getBytes(StandardCharsets.UTF_8)
        );

        Object profile;
        try {
            profile = Bukkit.class.getMethod(
                "createPlayerProfile", UUID.class, String.class
            ).invoke(null, profileId, "GoatSuperCore");
        } catch (NoSuchMethodException exception) {
            profile = Bukkit.class.getMethod(
                "createPlayerProfile", UUID.class
            ).invoke(null, profileId);
        }

        Object textures = profileClass.getMethod("getTextures").invoke(profile);
        URL skinUrl;
        try {
            skinUrl = new URL(TEXTURE_URL);
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("The Super Supreme Core texture URL is invalid.", exception);
        }
        texturesClass.getMethod("setSkin", URL.class).invoke(textures, skinUrl);
        profileClass.getMethod("setTextures", texturesClass).invoke(profile, textures);

        Method applyProfile;
        try {
            applyProfile = skullMetaClass.getMethod("setOwnerProfile", profileClass);
        } catch (NoSuchMethodException exception) {
            applyProfile = skullMetaClass.getMethod("setPlayerProfile", profileClass);
        }
        applyProfile.invoke(meta, profile);
        stack.setItemMeta(meta);
    }
}
