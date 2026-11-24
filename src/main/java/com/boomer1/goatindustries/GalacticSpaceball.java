package com.boomer1.goatindustries;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

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
        64, 64, 1
    };

    private GalacticSpaceball() {
    }

    public static void register(JavaPlugin plugin, ItemGroup itemGroup, SlimefunAddon addon) {
        SlimefunItemStack spaceballStack = new SlimefunItemStack(
            ITEM_ID,
            Material.PLAYER_HEAD,
            ChatColor.LIGHT_PURPLE + ITEM_NAME,
            ChatColor.GRAY + ITEM_LORE
        );
        setHeadTexture(spaceballStack);

        ItemStack[] recipe = resolveRecipe();
        new UnplaceableBlock(
            itemGroup,
            spaceballStack,
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
                    "Required Galactic Spaceball ingredient is not registered: " + id
                );
            }

            ingredients[slot] = item.getItem().clone();
            ingredients[slot].setAmount(RECIPE_AMOUNTS[slot]);
        }
        return ingredients;
    }

    private static void setHeadTexture(ItemStack stack) {
        if (!(stack.getItemMeta() instanceof SkullMeta skullMeta)) {
            throw new IllegalStateException("The Galactic Spaceball item is not a player head.");
        }

        UUID profileId = UUID.nameUUIDFromBytes(
            (ITEM_ID + ":" + ITEM_NAME).getBytes(StandardCharsets.UTF_8)
        );
        PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "GoatSpaceball");
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(TEXTURE_URL));
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("The Galactic Spaceball texture URL is invalid.", exception);
        }
        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);
        stack.setItemMeta(skullMeta);
    }
}
