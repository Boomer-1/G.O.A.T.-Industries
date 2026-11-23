package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class SuperDaxi {

    public static final String ITEM_ID = "GOAT_SUPER_DAXI";
    public static final String ITEM_NAME = "Super Daxi";
    public static final String ITEM_LORE = "Thanos has nothing on this";
    public static final String TEXTURE_URL =
        "http://textures.minecraft.net/texture/4e585aa4ad88775d337ecfc8a53e210ab316c33bd7836b26422051465f2ac06e";

    private static final String[] RECIPE_IDS = {
        "DAXI_ABSORPTION", null, "DAXI_FORTITUDE",
        null, "DAXI_SATURATION", null,
        "DAXI_REGENERATION", null, "DAXI_STRENGTH"
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

    private SuperDaxi() {
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
        ItemStack superDaxiStack = (ItemStack) stackConstructor.newInstance(
            ITEM_ID,
            Material.PLAYER_HEAD,
            ChatColor.LIGHT_PURPLE + ITEM_NAME,
            new String[] {ChatColor.GRAY + ITEM_LORE}
        );
        setHeadTexture(superDaxiStack, slimefunLoader);

        ItemStack[] recipe = resolveRecipe(slimefunLoader);
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);

        Constructor<?> itemConstructor = unplaceableBlockClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object superDaxi = itemConstructor.newInstance(
            itemGroup, superDaxiStack, enhancedCraftingTable, recipe
        );

        Class<?> addonClass = Class.forName(ADDON_CLASS, true, slimefunLoader);
        Method register = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader)
            .getMethod("register", addonClass);
        register.invoke(superDaxi, addon);

        plugin.getLogger().info(
            "[G.O.A.T. Industries] Super Daxi registered as a non-placeable item "
                + "with its five-slot Enhanced Crafting Table recipe."
        );
    }

    private static ItemStack[] resolveRecipe(ClassLoader slimefunLoader) throws ReflectiveOperationException {
        Class<?> slimefunItemClass = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader);
        Method getById = slimefunItemClass.getMethod("getById", String.class);
        Method getItem = slimefunItemClass.getMethod("getItem");

        ItemStack[] ingredients = new ItemStack[RECIPE_IDS.length];
        for (int slot = 0; slot < RECIPE_IDS.length; slot++) {
            String id = RECIPE_IDS[slot];
            if (id == null) {
                continue;
            }

            Object item = getById.invoke(null, id);
            if (item == null) {
                throw new IllegalStateException(
                    "Required Super Daxi ingredient is not registered: " + id
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
        Class<?> skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta", true, slimefunLoader);
        if (!skullMetaClass.isInstance(meta)) {
            throw new IllegalStateException("The Super Daxi item is not a player head.");
        }

        Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile", true, slimefunLoader);
        Class<?> texturesClass = Class.forName("org.bukkit.profile.PlayerTextures", true, slimefunLoader);
        UUID profileId = UUID.nameUUIDFromBytes(
            (ITEM_ID + ":" + ITEM_NAME).getBytes(StandardCharsets.UTF_8)
        );

        Object profile;
        try {
            profile = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class)
                .invoke(null, profileId, "GoatSuperDaxi");
        } catch (NoSuchMethodException exception) {
            profile = Bukkit.class.getMethod("createPlayerProfile", UUID.class).invoke(null, profileId);
        }

        Object textures = profileClass.getMethod("getTextures").invoke(profile);
        URL skinUrl;
        try {
            skinUrl = new URL(TEXTURE_URL);
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("The Super Daxi texture URL is invalid.", exception);
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
