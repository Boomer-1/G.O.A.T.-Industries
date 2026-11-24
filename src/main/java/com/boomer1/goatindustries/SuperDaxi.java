package com.boomer1.goatindustries;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private SuperDaxi() {
    }

    public static void register(JavaPlugin plugin, ItemGroup itemGroup, SlimefunAddon addon) {
        SlimefunItemStack superDaxiStack = new SlimefunItemStack(
            ITEM_ID,
            Material.PLAYER_HEAD,
            ChatColor.LIGHT_PURPLE + ITEM_NAME,
            ChatColor.GRAY + ITEM_LORE
        );
        setHeadTexture(superDaxiStack);

        ItemStack[] recipe = resolveRecipe();
        new UnplaceableBlock(
            itemGroup,
            superDaxiStack,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            recipe
        ).register(addon);
    }

    private static ItemStack[] resolveRecipe() {
        ItemStack[] ingredients = new ItemStack[RECIPE_IDS.length];
        for (int slot = 0; slot < RECIPE_IDS.length; slot++) {
            String id = RECIPE_IDS[slot];
            if (id == null) {
                continue;
            }

            SlimefunItem item = SlimefunItem.getById(id);
            if (item == null) {
                throw new IllegalStateException(
                    "Required Super Daxi ingredient is not registered: " + id
                );
            }

            ingredients[slot] = item.getItem().clone();
            ingredients[slot].setAmount(64);
        }
        return ingredients;
    }

    private static void setHeadTexture(ItemStack stack) {
        if (!(stack.getItemMeta() instanceof SkullMeta skullMeta)) {
            throw new IllegalStateException("The Super Daxi item is not a player head.");
        }

        UUID profileId = UUID.nameUUIDFromBytes(
            (ITEM_ID + ":" + ITEM_NAME).getBytes(StandardCharsets.UTF_8)
        );
        PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "GoatSuperDaxi");
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(TEXTURE_URL));
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("The Super Daxi texture URL is invalid.", exception);
        }
        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);
        stack.setItemMeta(skullMeta);
    }
}
