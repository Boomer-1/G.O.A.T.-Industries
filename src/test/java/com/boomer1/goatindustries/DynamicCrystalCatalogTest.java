package com.boomer1.goatindustries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class DynamicCrystalCatalogTest {

    @Test
    void definesSixOutputsInRarityOrder() {
        assertEquals(
            List.of("COMMON", "UNCOMMON", "RARE", "EPIC", "MYTHICAL", "UNIQUE"),
            DynamicCrystalCatalog.OUTPUTS.stream().map(DynamicCrystalCatalog.CrystalOutput::rarity).toList()
        );
    }

    @Test
    void everyOutputUsesNineCrystalTypesInAlphabeticalOrder() {
        assertEquals(
            List.of(
                "ALCHEMICAL", "ANIMAL", "CELESTIAL", "ELEMENTAL", "HISTORICAL",
                "HUMAN", "MECHANICAL", "PHILOSOPHICAL", "VOID"
            ),
            DynamicCrystalCatalog.CRYSTAL_TYPES
        );
        for (DynamicCrystalCatalog.CrystalOutput output : DynamicCrystalCatalog.OUTPUTS) {
            List<String> ids = DynamicCrystalCatalog.inputIds(output.rarity());
            assertEquals(9, ids.size());
            assertEquals("CRY_CRYSTAL_" + output.rarity() + "_ALCHEMICAL", ids.get(0));
            assertEquals("CRY_CRYSTAL_" + output.rarity() + "_VOID", ids.get(8));
        }
    }

    @Test
    void definesOmegaSupercrystalRecipeAndApprovedLore() {
        assertEquals("Harvested power from the crystals of the world",
            DynamicCrystalCatalog.DYNAMIC_CRYSTAL_LORE);
        assertEquals("GOAT_OMEGA_SUPERCRYSTAL", DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_ID);
        assertEquals("Omega Supercrystal", DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_NAME);
        assertEquals("The Omega crystal is the ultimate power driver.",
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_LORE);
        assertEquals(
            "https://textures.minecraft.net/texture/609d6a348a9923ebbb3423b196973d495b1aa5c056e4b351b120d36d8331a542",
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_TEXTURE
        );
        assertEquals(
            List.of(
                "GOAT_DYNAMIC_COMMON_CRYSTAL",
                "GOAT_DYNAMIC_UNIQUE_CRYSTAL",
                "GOAT_DYNAMIC_UNCOMMON_CRYSTAL",
                "GOAT_DYNAMIC_RARE_CRYSTAL",
                "GOAT_DYNAMIC_EPIC_CRYSTAL",
                "GOAT_DYNAMIC_MYTHICAL_CRYSTAL"
            ),
            DynamicCrystalCatalog.OMEGA_SUPERCRYSTAL_INPUT_IDS
        );
    }

    @Test
    void usesTheApprovedMachineRecipeAndEnergySettings() {
        assertEquals(
            List.of(
                "IE_VOID_SMELTERY", "IE_QUARRY_3", "IE_VOID_SMELTERY",
                "SUPREME_ELECTRIC_CORE_III", "IE_VOID_CAPACITOR", "SUPREME_ELECTRIC_CORE_III",
                "CRY_EXALTED_BEACON", "LAB_LASER_DRILL", "PLUTONIUM"
            ),
            DynamicCrystalCatalog.MACHINE_RECIPE_IDS
        );
        assertEquals(16_000_000, DynamicCrystalCatalog.ENERGY_CAPACITY);
        assertEquals(500_000, DynamicCrystalCatalog.ENERGY_PER_TICK);
        assertEquals("DEEPSLATE_TILES", DynamicCrystalCatalog.MACHINE_MATERIAL);
        assertEquals(30, DynamicCrystalCatalog.PROCESSING_SECONDS);
    }

    @Test
    void allSixTextureUrlsArePresentAndUnique() {
        assertEquals(6, DynamicCrystalCatalog.OUTPUTS.stream()
            .map(DynamicCrystalCatalog.CrystalOutput::textureUrl)
            .distinct()
            .count());
        assertTrue(DynamicCrystalCatalog.OUTPUTS.stream()
            .allMatch(output -> output.textureUrl().startsWith("https://textures.minecraft.net/texture/")));
    }
}
