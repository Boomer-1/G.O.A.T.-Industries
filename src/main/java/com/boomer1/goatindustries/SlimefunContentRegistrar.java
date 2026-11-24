package com.boomer1.goatindustries;

import java.io.IOException;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SlimefunContentRegistrar {

    private SlimefunContentRegistrar() {
    }

    public static void register(JavaPlugin plugin, Plugin emcTech)
        throws ReflectiveOperationException, IOException {
        SlimefunAddon addon = (SlimefunAddon) plugin;

        suppressRuntimeRegistrationLog(plugin.getLogger());

        ItemStack groupIcon = new ItemStack(Material.REINFORCED_DEEPSLATE);
        ItemMeta groupMeta = groupIcon.getItemMeta();
        groupMeta.setDisplayName(ChatColor.GOLD + "G.O.A.T. Industries");
        groupIcon.setItemMeta(groupMeta);

        NestedItemGroup itemGroup = new NestedItemGroup(
            new NamespacedKey(plugin, "goat_industries"),
            groupIcon
        );

        SubItemGroup trophyGroup = createSubGroup(
            plugin, itemGroup, "goat_trophy", Material.GOAT_SPAWN_EGG, ChatColor.GOLD + "The Goat Trophy"
        );
        SubItemGroup machinesGroup = createSubGroup(
            plugin, itemGroup, "machines", Material.BLAST_FURNACE, ChatColor.GOLD + "Machines"
        );
        SubItemGroup resourcesGroup = createSubGroup(
            plugin, itemGroup, "resources", Material.DIAMOND, ChatColor.GOLD + "Resources"
        );

        NamespacedKey trophyMarker = new NamespacedKey(plugin, "goat_trophy");
        SlimefunItemStack trophyStack = createSlimefunStack(
            "GOAT_TROPHY",
            Material.GOAT_SPAWN_EGG,
            ChatColor.GOLD + "G.O.A.T. Trophy",
            ChatColor.GRAY + "The ultimate trophy.",
            ChatColor.GRAY + "Requires a five-tier Netherite pyramid.",
            ChatColor.YELLOW + "Right-click the top Netherite Block to place."
        );
        ItemMeta trophyMeta = trophyStack.getItemMeta();
        trophyMeta.getPersistentDataContainer().set(trophyMarker, PersistentDataType.BYTE, (byte) 1);
        trophyStack.setItemMeta(trophyMeta);

        SlimefunItemStack guideStack = createSlimefunStack(
            "GOAT_PYRAMID_INSTRUCTIONS",
            Material.BOOK,
            ChatColor.AQUA + "G.O.A.T. Pyramid Instructions",
            ChatColor.GRAY + "Build five centered square layers",
            ChatColor.GRAY + "using Netherite Blocks.",
            ChatColor.WHITE + "Top to bottom:",
            ChatColor.YELLOW + "1 x 1  -   1 block",
            ChatColor.YELLOW + "3 x 3  -   9 blocks",
            ChatColor.YELLOW + "5 x 5  -  25 blocks",
            ChatColor.YELLOW + "7 x 7  -  49 blocks",
            ChatColor.YELLOW + "9 x 9  -  81 blocks",
            ChatColor.GOLD + "Total: 165 Netherite Blocks",
            ChatColor.GRAY + "Center every layer above the previous layer.",
            ChatColor.GREEN + "Hold the G.O.A.T. Trophy and",
            ChatColor.GREEN + "right-click the single top block.",
            ChatColor.DARK_RED + "Guide item only - not a crafting ingredient."
        );

        SlimefunItemStack emcDiamondStack = createSlimefunStack(
            "EMC_DIAMOND",
            Material.DIAMOND_BLOCK,
            ChatColor.AQUA + "EMC Diamond",
            ChatColor.GRAY + "A reward for reaching",
            ChatColor.GOLD + "1,000,000,000 EMC.",
            ChatColor.GRAY + "Awarded once per player."
        );

        new SlimefunItem(trophyGroup, guideStack, RecipeType.NULL, new ItemStack[0]).register(addon);
        new SlimefunItem(resourcesGroup, emcDiamondStack, RecipeType.NULL, new ItemStack[0]).register(addon);

        UniversalPowerCore.register(plugin, resourcesGroup, addon);
        GalacticSpaceball.register(plugin, resourcesGroup, addon);
        DynamicCrystalFormer.register(plugin, machinesGroup, resourcesGroup, addon);
        NuclearCompressor.register(plugin, machinesGroup, resourcesGroup, addon);
        ImplosivePress.register(plugin, machinesGroup, resourcesGroup, addon);
        SuperSupremeCore.register(plugin, resourcesGroup, addon);
        SuperDaxi.register(plugin, resourcesGroup, addon);

        ItemStack[] trophyRecipe = resolveRecipe(new String[] {
            "GOAT_SUPER_DAXI",
            "IE_INFINITY_SINGULARITY_REACTOR",
            "GOAT_SUPER_SUPREME_CORE",
            "GOAT_IMPLOSIVE_COBBLESTONE",
            "GOAT_NUCLEAR_UU_MATTER",
            "GOAT_GALACTIC_SPACEBALL",
            "GOAT_UNIVERSAL_POWER_CORE",
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_ID,
            "EMC_DIAMOND"
        });
        new SlimefunItem(
            trophyGroup,
            trophyStack,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            trophyRecipe
        ).register(addon);

        registerResearches(plugin);

        plugin.getServer().getPluginManager().registerEvents(new DynamicCrystalPlacementListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GoatTrophyListener(plugin), plugin);
        new EmcDiamondRewardManager(plugin, emcTech, emcDiamondStack).start();
    }

    private static void registerResearches(JavaPlugin plugin) {
        registerResearch(
            plugin, 2_026_110_001, "goat_trophy", "G.O.A.T. Trophy", 200,
            "GOAT_TROPHY"
        );
        registerResearch(
            plugin, 2_026_110_002, "nuclear_compressor", "Nuclear Compressor", 50,
            NuclearCompressor.MACHINE_ID
        );
        registerResearch(
            plugin, 2_026_110_003, "implosive_press", "Implosive Press", 50,
            ImplosivePress.MACHINE_ID
        );
        registerResearch(
            plugin, 2_026_110_004, "dynamic_crystal_former", "Dynamic Crystal Former", 50,
            DynamicCrystalCatalog.MACHINE_ID
        );
        registerResearch(
            plugin, 2_026_110_005, "emc_diamond", "EMC Diamond", 50,
            "EMC_DIAMOND"
        );
        registerResearch(
            plugin, 2_026_110_006, "universal_power_core", "Universal Power Core", 40,
            UniversalPowerCore.ITEM_ID
        );
        registerResearch(
            plugin, 2_026_110_007, "galactic_spaceball", "Galactic Spaceball", 40,
            GalacticSpaceball.ITEM_ID
        );

        String[] dynamicCrystalIds = new String[DynamicCrystalCatalog.OUTPUTS.size() + 1];
        for (int i = 0; i < DynamicCrystalCatalog.OUTPUTS.size(); i++) {
            dynamicCrystalIds[i] = DynamicCrystalCatalog.OUTPUTS.get(i).id();
        }
        dynamicCrystalIds[dynamicCrystalIds.length - 1] = DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_ID;
        registerResearch(
            plugin, 2_026_110_008, "dynamic_crystals", "Dynamic Crystals and Omega Supercrystal", 30,
            dynamicCrystalIds
        );

        registerResearch(
            plugin, 2_026_110_009, "nuclear_uu_matter", "Nuclear UU Matter", 75,
            NuclearCompressor.OUTPUT_ID
        );
        registerResearch(
            plugin, 2_026_110_010, "implosive_cobblestone", "Implosive Cobblestone", 40,
            ImplosivePress.OUTPUT_ID
        );
        registerResearch(
            plugin, 2_026_110_011, "super_supreme_core", "Super Supreme Core", 50,
            SuperSupremeCore.ITEM_ID
        );
        registerResearch(
            plugin, 2_026_110_012, "supreme_daxi", "Supreme Daxi", 75,
            SuperDaxi.ITEM_ID
        );
    }

    private static void registerResearch(
        JavaPlugin plugin,
        int researchId,
        String key,
        String name,
        int xpLevels,
        String... itemIds
    ) {
        Research research = new Research(
            new NamespacedKey(plugin, key),
            researchId,
            name,
            xpLevels
        );

        for (String itemId : itemIds) {
            SlimefunItem item = SlimefunItem.getById(itemId);
            if (item == null) {
                throw new IllegalStateException(
                    "Cannot register research '" + name + "' because item is not registered: " + itemId
                );
            }
            research.addItems(item);
        }

        research.register();
    }

    private static void suppressRuntimeRegistrationLog(Logger logger) {
        Filter existingFilter = logger.getFilter();
        logger.setFilter(record -> {
            String message = record.getMessage();
            if (record.getLevel().intValue() == Level.INFO.intValue()
                && message != null
                && message.endsWith(": Item was registered during runtime.")) {
                return false;
            }
            return existingFilter == null || existingFilter.isLoggable(record);
        });
    }

    private static SubItemGroup createSubGroup(
        JavaPlugin plugin,
        NestedItemGroup parent,
        String key,
        Material icon,
        String displayName
    ) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return new SubItemGroup(new NamespacedKey(plugin, key), parent, item);
    }

    private static ItemStack[] resolveRecipe(String[] itemIds) {
        ItemStack[] recipe = new ItemStack[itemIds.length];
        for (int slot = 0; slot < itemIds.length; slot++) {
            SlimefunItem item = SlimefunItem.getById(itemIds[slot]);
            if (item == null) {
                throw new IllegalStateException(
                    "Required G.O.A.T. Trophy ingredient is not registered: " + itemIds[slot]
                );
            }
            recipe[slot] = item.getItem().clone();
        }
        return recipe;
    }

    private static SlimefunItemStack createSlimefunStack(
        String id,
        Material material,
        String displayName,
        String... lore
    ) {
        return new SlimefunItemStack(id, material, displayName, lore);
    }
}
