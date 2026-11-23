package com.boomer1.goatindustries;

import java.util.List;

public final class DynamicCrystalCatalog {

    public static final String MACHINE_ID = "GOAT_DYNAMIC_CRYSTAL_FORMER";
    public static final String MACHINE_NAME = "Dynamic Crystal Former";
    public static final String MACHINE_MATERIAL = "DEEPSLATE_TILES";
    public static final String MACHINE_LORE = "Harnesses the power of the Crystae";

    public static final List<String> CRYSTAL_TYPES = List.of(
        "ALCHEMICAL",
        "ANIMAL",
        "CELESTIAL",
        "ELEMENTAL",
        "HISTORICAL",
        "HUMAN",
        "MECHANICAL",
        "PHILOSOPHICAL",
        "VOID"
    );

    public static final List<String> MACHINE_RECIPE_IDS = List.of(
        "IE_VOID_SMELTERY",
        "IE_QUARRY_3",
        "IE_VOID_SMELTERY",
        "SUPREME_ELECTRIC_CORE_III",
        "IE_VOID_CAPACITOR",
        "SUPREME_ELECTRIC_CORE_III",
        "CRY_EXALTED_BEACON",
        "LAB_LASER_DRILL",
        "PLUTONIUM"
    );

    public static final List<CrystalOutput> OUTPUTS = List.of(
        new CrystalOutput("COMMON", "GOAT_DYNAMIC_COMMON_CRYSTAL", "Dynamic Common Crystal",
            "https://textures.minecraft.net/texture/9be74f191e8aed6d7c2e45f97e798433e9ea40146f6498f844d4ecefe76392f"),
        new CrystalOutput("UNCOMMON", "GOAT_DYNAMIC_UNCOMMON_CRYSTAL", "Dynamic Uncommon Crystal",
            "https://textures.minecraft.net/texture/17521ed24eb1e764442f13f5c02352ec04d3c35c36cca56b6c36487ec9189002"),
        new CrystalOutput("RARE", "GOAT_DYNAMIC_RARE_CRYSTAL", "Dynamic Rare Crystal",
            "https://textures.minecraft.net/texture/969c103338745fa1b1832d66bc95f9c7e3110cb63abe22ff1c2e9b2eca1aeed6"),
        new CrystalOutput("EPIC", "GOAT_DYNAMIC_EPIC_CRYSTAL", "Dynamic Epic Crystal",
            "https://textures.minecraft.net/texture/d0352a7a16d3edd503218a1db81db0793c225de8c4493db0b29c0daec6db40a5"),
        new CrystalOutput("MYTHICAL", "GOAT_DYNAMIC_MYTHICAL_CRYSTAL", "Dynamic Mythical Crystal",
            "https://textures.minecraft.net/texture/32768a4dfd517d7aa52fe70bf29a741e551d4108379a34e3d5b0dcc658c54a73"),
        new CrystalOutput("UNIQUE", "GOAT_DYNAMIC_UNIQUE_CRYSTAL", "Dynamic Unique Crystal",
            "https://textures.minecraft.net/texture/2024f3c5efa8dca8197cd7847e3f4103f9ab7d2b8044fb1833738a3e0bcc85b8")
    );

    public static final int ENERGY_CAPACITY = 16_000_000;
    public static final int ENERGY_PER_TICK = 500_000;
    public static final int PROCESSING_TICKS = 600;
    public static final int OUTPUT_SLOT = 16;
    public static final int PROGRESS_SLOT = 22;

    private static final int[] INPUT_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    private DynamicCrystalCatalog() {
    }

    public static int[] inputSlots() {
        return INPUT_SLOTS.clone();
    }

    public static List<String> inputIds(String rarity) {
        return CRYSTAL_TYPES.stream().map(type -> "CRY_CRYSTAL_" + rarity + "_" + type).toList();
    }

    public record CrystalOutput(String rarity, String id, String displayName, String textureUrl) {
    }
}
