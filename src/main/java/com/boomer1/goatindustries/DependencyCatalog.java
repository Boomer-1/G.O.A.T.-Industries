package com.boomer1.goatindustries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DependencyCatalog {

    public static final String REQUIRED_PLUGIN = "EMCTech";

    private static final List<ItemRequirement> ITEMS = buildItems();

    private DependencyCatalog() {
    }

    public static List<ItemRequirement> items() {
        return ITEMS;
    }

    private static List<ItemRequirement> buildItems() {
        List<ItemRequirement> items = new ArrayList<>();

        add(items, "InfinityExpansion2", "Infinity Singularity Reactor", "IE_INFINITY_SINGULARITY_REACTOR");
        add(items, "InfinityExpansion2", "Infinity Capacitor", "IE_INFINITY_CAPACITOR");

        add(items, "TranscEndence", "Daxi (S) - Strength", "DAXI_STRENGTH");
        add(items, "TranscEndence", "Daxi (A) - Absorption", "DAXI_ABSORPTION");
        add(items, "TranscEndence", "Daxi (F) - Fortitude", "DAXI_FORTITUDE");
        add(items, "TranscEndence", "Daxi (H) - Saturation", "DAXI_SATURATION");
        add(items, "TranscEndence", "Daxi (R) - Regeneration", "DAXI_REGENERATION");

        add(items, "Supreme", "Core of Life", "SUPREME_CORE_OF_LIFE");
        add(items, "Supreme", "Core of Death", "SUPREME_CORE_OF_DEATH");
        add(items, "Supreme", "Core of Color", "SUPREME_CORE_OF_COLOR");
        add(items, "Supreme", "Core of Block", "SUPREME_CORE_OF_BLOCK");
        add(items, "Supreme", "Core of Nature", "SUPREME_CORE_OF_NATURE");
        add(items, "Supreme", "Core of Alloy", "SUPREME_CORE_OF_ALLOY");
        add(items, "Supreme", "Supreme", "SUPREME_SUPREME");
        add(items, "Supreme", "Supreme Nugget", "SUPREME_SUPREME_NUGGET");
        add(items, "Supreme", "Thornium Ingot", "SUPREME_THORNIUM_INGOT");
        add(items, "Supreme", "Supreme Attribute Bomb", "SUPREME_ATTRIBUTE_BOMB");
        add(items, "Supreme", "Supreme Capacitor", "SUPREME_SUPREME_CAPACITOR");
        add(items, "Supreme", "Supreme Generator", "SUPREME_SUPREME_GENERATOR");

        for (int level = 0; level < 50; level++) {
            add(items, "MomoTech", "Compressed Cobblestone Level " + (level + 1),
                "MOMOTECH_COBBLESTONE" + level);
        }

        add(items, "LiteXpansion", "UU Matter", "UU_MATTER");

        add(items, "Galactifun", "Laserite Ore", "LASERITE_ORE");
        add(items, "Galactifun", "Moon Rock", "MOON_ROCK");
        add(items, "Galactifun", "Mars Rock", "MARS_ROCK");
        add(items, "Galactifun", "Dry Ice", "DRY_ICE");
        add(items, "Galactifun", "Methane Ice", "METHANE_ICE");
        add(items, "Galactifun", "Sulfur Block", "SULFUR_BLOCK");
        add(items, "Galactifun", "Ventstone", "VENTSTONE");
        add(items, "Galactifun", "Diamond Anvil Cell", "DIAMOND_ANVIL_CELL");
        add(items, "Galactifun", "Stargate Controller", "STARGATE_CONTROLLER");

        add(items, "Tsingshan Technology", "PRO Generator", "QS_PRO_FADIANJI_FDL");

        add(items, "FNAmplifications", "FN Generator R12", "FN_POWER_XPANSION_POWER_R12");

        add(items, "DankTech2", "Dank Pack Tier 9", "DK2_PACK_9");

        add(items, "Networks", "Quantum Storage 140.7T", "NTW_QUANTUM_STORAGE_14");

        String[] rarities = {"COMMON", "UNCOMMON", "RARE", "EPIC", "MYTHICAL", "UNIQUE"};
        String[] crystalTypes = {
            "ELEMENTAL", "MECHANICAL", "ALCHEMICAL", "HISTORICAL", "HUMAN",
            "ANIMAL", "CELESTIAL", "VOID", "PHILOSOPHICAL"
        };

        for (String rarity : rarities) {
            for (String crystalType : crystalTypes) {
                String id = "CRY_CRYSTAL_" + rarity + "_" + crystalType;
                String displayName = titleCase(rarity) + " " + titleCase(crystalType) + " Crystal";
                add(items, "Crystamae Historia", displayName, id);
            }
        }

        Set<String> uniqueIds = new HashSet<>();
        for (ItemRequirement item : items) {
            if (!uniqueIds.add(item.id())) {
                throw new IllegalStateException("Duplicate Slimefun item ID in dependency catalog: " + item.id());
            }
        }

        return List.copyOf(items);
    }

    private static void add(List<ItemRequirement> items, String addon, String displayName, String id) {
        items.add(new ItemRequirement(addon, displayName, id));
    }

    private static String titleCase(String value) {
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public record ItemRequirement(String addon, String displayName, String id) {
    }
}
