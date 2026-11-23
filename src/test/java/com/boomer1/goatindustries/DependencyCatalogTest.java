package com.boomer1.goatindustries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DependencyCatalogTest {

    @Test
    void catalogContainsAllFourteenAddons() {
        assertEquals(14, DependencyCatalog.addons().size());

        Set<String> addonNames = new HashSet<>();
        DependencyCatalog.addons().forEach(addon -> assertTrue(addonNames.add(addon.name()),
            "Duplicate addon requirement: " + addon.name()));
    }

    @Test
    void catalogContainsAllCollectedRequirementsWithoutDuplicateIds() {
        assertEquals(153, DependencyCatalog.items().size());

        Set<String> ids = new HashSet<>();
        DependencyCatalog.items().forEach(item -> assertTrue(ids.add(item.id()),
            "Duplicate dependency ID: " + item.id()));
    }

    @Test
    void catalogContainsLambdaBatteryForUltimateGeneratorsTwo() {
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.addon().equals("Ultimate Generators 2")
                && item.displayName().equals("LAMBDA-λ Electricity Storage")
                && item.id().equals("LAMBDA_ELECTRICITY_STORAGE")));
    }


    @Test
    void catalogContainsLiteXpansionMachines() {
        assertHasItem("LiteXpansion", "SCRAP_MACHINE");
        assertHasItem("LiteXpansion", "MASS_FABRICATOR_MACHINE");
    }

    @Test
    void catalogContainsNewInfinityExpansionTwoMachines() {
        assertHasItem("InfinityExpansion2", "IE_SINGULARITY_CONSTRUCTOR_2");
        assertHasItem("InfinityExpansion2", "IE_SINGULARITY_CONSTRUCTOR");
        assertHasItem("InfinityExpansion2", "IE_INFINITY_SINGULARITY_REACTOR");
        assertHasItem("InfinityExpansion2", "IE_INFINITY_CAPACITOR");
        assertHasItem("InfinityExpansion2", "IE_INFINITY_WORKBENCH");
        assertHasItem("InfinityExpansion2", "IE_VOID_SMELTERY");
        assertHasItem("InfinityExpansion2", "IE_QUARRY_3");
        assertHasItem("InfinityExpansion2", "IE_VOID_CAPACITOR");
    }

    @Test
    void catalogContainsNewSupremeQuarries() {
        assertHasItem("Supreme", "SUPREME_THORNIUM_QUARRY");
        assertHasItem("Supreme", "SUPREME_SUPREME_NUGGETS_QUARRY");
        assertHasItem("Supreme", "SUPREME_THORNIUM_ENERGIZED");
    }

    private static void assertHasItem(String addon, String id) {
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.addon().equals(addon) && item.id().equals(id)),
            "Missing dependency " + id + " for " + addon);
    }

    @Test
    void catalogContainsDynamicCrystalFormerIngredients() {
        assertHasItem("Supreme", "SUPREME_ELECTRIC_CORE_III");
        assertHasItem("Crystamae Historia", "CRY_EXALTED_BEACON");
        assertHasItem("Laboratory", "LAB_LASER_DRILL");
        assertHasItem("Slimefun", "PLUTONIUM");
    }

    @Test
    void catalogContainsAllFiftyMomotechCobblestoneLevels() {
        long count = DependencyCatalog.items().stream()
            .filter(item -> item.addon().equals("MomoTech"))
            .count();

        assertEquals(50, count);
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.id().equals("MOMOTECH_COBBLESTONE0")));
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.id().equals("MOMOTECH_COBBLESTONE49")));
    }

    @Test
    void catalogContainsAllFiftyFourCrystamaeCrystals() {
        long count = DependencyCatalog.items().stream()
            .filter(item -> item.id().startsWith("CRY_CRYSTAL_"))
            .count();

        assertEquals(54, count);
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.id().equals("CRY_CRYSTAL_COMMON_ELEMENTAL")));
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.id().equals("CRY_CRYSTAL_UNIQUE_PHILOSOPHICAL")));
    }
}
