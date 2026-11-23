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
