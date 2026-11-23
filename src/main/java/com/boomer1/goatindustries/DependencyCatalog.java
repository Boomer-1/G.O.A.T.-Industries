package com.boomer1.goatindustries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DependencyCatalog {

    private static final List<AddonRequirement> ADDONS = List.of(
        new AddonRequirement("InfinityExpansion2", "InfinityExpansion2"),
        new AddonRequirement("TranscEndence", "TranscEndence"),
        new AddonRequirement("Supreme", "Supreme"),
        new AddonRequirement("MomoTech", "MomoTech"),
        new AddonRequirement("LiteXpansion", "LiteXpansion"),
        new AddonRequirement("Galactifun", "Galactifun"),
        new AddonRequirement("Tsingshan Technology", "TsingshanTechnology-Fixed"),
        new AddonRequirement("Ultimate Generators 2", "UltimateGenerators2"),
        new AddonRequirement("FNAmplifications", "FNAmplifications"),
        new AddonRequirement("DankTech2", "DankTech2"),
        new AddonRequirement("Networks", "Networks"),
        new AddonRequirement("Crystamae Historia", "CrystamaeHistoria"),
        new AddonRequirement("Laboratory", "Laboratory"),
        new AddonRequirement("EMCTech", "EMCTech")
    );

    private static final List<ItemRequirement> CORE_ITEMS = List.of(
        new ItemRequirement("Slimefun", "Plutonium", "PLUTONIUM")
    );

    private static final List<ItemRequirement> ITEMS = buildItems();
    private static final List<ItemRequirement> ALL_ITEMS = buildAllItems();

    private DependencyCatalog() {
    }

    public static List<AddonRequirement> addons() {
        return ADDONS;
    }

    public static List<ItemRequirement> items() {
        return ALL_ITEMS;
    }

    public static List<ItemRequirement> coreItems() {
        return CORE_ITEMS;
    }

    public static List<ItemRequirement> itemsForAddon(String addon) {
        return ALL_ITEMS.stream()
            .filter(item -> item.addon().equals(addon))
            .toList();
    }

    private static List<ItemRequirement> buildItems() {
        List<ItemRequirement> items = new ArrayList<>();

        add(items, "InfinityExpansion2", "Infinity Singularity Constructor", "IE_SINGULARITY_CONSTRUCTOR_2");
        add(items, "InfinityExpansion2", "Singularity Constructor", "IE_SINGULARITY_CONSTRUCTOR");
        add(items, "InfinityExpansion2", "Void Smeltery", "VOID_SMELTERY");
        add(items, "InfinityExpansion2", "Void Quarry", "QUARRY_3");
        add(items, "InfinityExpansion2", "Void Capacitor", "VOID_CAPACITOR");

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
        add(items, "Supreme", "Thornium Quarry", "SUPREME_THORNIUM_QUARRY");
        add(items, "Supreme", "Nuggets of Supreme Quarry", "SUPREME_SUPREME_NUGGETS_QUARRY");
        add(items, "Supreme", "Electric Core Fabricator III", "SUPREME_ELECTRIC_CORE_III");

        for (int level = 0; level < 50; level++) {
            add(items, "MomoTech", "Compressed Cobblestone Level " + (level + 1),
                "MOMOTECH_COBBLESTONE" + level);
        }

        add(items, "LiteXpansion", "UU Matter", "UU_MATTER");
        add(items, "LiteXpansion", "Recycler", "SCRAP_MACHINE");
        add(items, "LiteXpansion", "Mass Fabricator", "MASS_FABRICATOR_MACHINE");

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
        add(items, "Laboratory", "Laser Drill", "LAB_LASER_DRILL");

        add(items, "Ultimate Generators 2", "LAMBDA-λ Electricity Storage", "LAMBDA_ELECTRICITY_STORAGE");

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
        add(items, "Crystamae Historia", "Exalted Beacon", "CRY_EXALTED_BEACON");

        Set<String> uniqueIds = new HashSet<>();
        for (ItemRequirement item : items) {
            if (!uniqueIds.add(item.id())) {
                throw new IllegalStateException("Duplicate Slimefun item ID in dependency catalog: " + item.id());
            }
        }

        return List.copyOf(items);
    }

    private static List<ItemRequirement> buildAllItems() {
        List<ItemRequirement> allItems = new ArrayList<>(ITEMS);
        allItems.addAll(CORE_ITEMS);
        return List.copyOf(allItems);
    }

    private static void add(List<ItemRequirement> items, String addon, String displayName, String id) {
        items.add(new ItemRequirement(addon, displayName, id));
    }

    private static String titleCase(String value) {
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public record AddonRequirement(String name, String pluginName) {
    }

    public record ItemRequirement(String addon, String displayName, String id) {
    }
}
