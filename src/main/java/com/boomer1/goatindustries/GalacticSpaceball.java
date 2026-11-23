package com.boomer1.goatindustries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class GalacticSpaceball {

    public static final String ITEM_ID = "GOAT_GALACTIC_SPACEBALL";
    public static final String ITEM_NAME = "Galactic Spaceball";
    public static final String ITEM_LORE =
        "Only Nixon could go to China, I made you go everywhere.";
    public static final String TEXTURE_URL =
        "https://textures.minecraft.net/texture/18aab83212ce019dd5377d9caf24da3a059c821b3fb6e8533659751f6d0f79a3";

    private static final List<String> RECIPE_IDS = List.of(
        "LASERITE_ORE",
        "MOON_ROCK",
        "MARS_ROCK",
        "DRY_ICE",
        "STARGATE_CONTROLLER",
        "METHANE_ICE",
        "SULFUR_BLOCK",
        "VENTSTONE",
        "DIAMOND_ANVIL_CELL"
    );

    private static final int[] RECIPE_AMOUNTS = {
        64, 64, 64,
        64, 1, 64,
        64, 64, 64
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

    private GalacticSpaceball() {
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
        ItemStack spaceballStack = (ItemStack) stackConstructor.newInstance(
            ITEM_ID,
            Material.PLAYER_HEAD,
            ChatColor.LIGHT_PURPLE + ITEM_NAME,
            new String[] {ChatColor.GRAY + ITEM_LORE}
        );
        setHeadTexture(spaceballStack, slimefunLoader);

        ItemStack[] recipe = resolveRecipe(slimefunLoader);
        Object enhancedCraftingTable = recipeTypeClass.getField("ENHANCED_CRAFTING_TABLE").get(null);

        Constructor<?> itemConstructor = unplaceableBlockClass.getConstructor(
            itemGroupClass, itemStackClass, recipeTypeClass, ItemStack[].class
        );
        Object spaceball = itemConstructor.newInstance(
            itemGroup, spaceballStack, enhancedCraftingTable, recipe
        );

        Class<?> addonClass = Class.forName(ADDON_CLASS, true, slimefunLoader);
        Method register = Class.forName(SLIMEFUN_ITEM_CLASS, true, slimefunLoader)
            .getMethod("register", addonClass);
        register.invoke(spaceball, addon);

        plugin.getLogger().info("[G.O.A.T. Industries] Galactic Spaceball registered as an unplaceable item "
            + "with its exact 3x3 Enhanced Crafting Table recipe.");
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
                throw new IllegalStateException(
                    "Required Galactic Spaceball ingredient is not registered: " + id
                );
            }

            ingredients[slot] = ((ItemStack) getItem.invoke(item)).clone();
            ingredients[slot].setAmount(RECIPE_AMOUNTS[slot]);
        }
        return ingredients;
    }

    private static void setHeadTexture(ItemStack stack, ClassLoader slimefunLoader)
        throws ReflectiveOperationException {

        ItemMeta meta = stack.getItemMeta();
        Class<?> skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta", true, slimefunLoader);
        if (!skullMetaClass.isInstance(meta)) {
            throw new IllegalStateException("The Galactic Spaceball item is not a player head.");
        }

        Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile", true, slimefunLoader);
        Class<?> texturesClass = Class.forName("org.bukkit.profile.PlayerTextures", true, slimefunLoader);
        UUID profileId = UUID.nameUUIDFromBytes(
            (ITEM_ID + ":" + ITEM_NAME).getBytes(StandardCharsets.UTF_8)
        );

        Object profile;
        try {
            profile = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class)
                .invoke(null, profileId, "GalacticSpaceball");
        } catch (NoSuchMethodException exception) {
            profile = Bukkit.class.getMethod("createPlayerProfile", UUID.class).invoke(null, profileId);
        }

        Object textures = profileClass.getMethod("getTextures").invoke(profile);
        texturesClass.getMethod("setSkin", URL.class).invoke(textures, new URL(TEXTURE_URL));
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
