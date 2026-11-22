package com.boomer1.goatindustries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DependencyCatalogTest {

    @Test
    void catalogContainsAllThirteenAddons() {
        assertEquals(13, DependencyCatalog.addons().size());

        Set<String> addonNames = new HashSet<>();
        DependencyCatalog.addons().forEach(addon -> assertTrue(addonNames.add(addon.name()),
            "Duplicate addon requirement: " + addon.name()));
    }

    @Test
    void catalogContainsAllCollectedRequirementsWithoutDuplicateIds() {
        assertEquals(143, DependencyCatalog.items().size());

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
    void catalogContainsNewInfinityExpansionTwoMachines() {
        assertHasItem("InfinityExpansion2", "IE_INFINITY_WORKBENCH");
        assertHasItem("InfinityExpansion2", "IE_SINGULARITY_CONSTRUCTOR_2");
        assertHasItem("InfinityExpansion2", "IE_SINGULARITY_CONSTRUCTOR");
    }

    @Test
    void catalogContainsNewSupremeQuarries() {
        assertHasItem("Supreme", "SUPREME_THORNIUM_QUARRY");
        assertHasItem("Supreme", "SUPREME_SUPREME_NUGGETS_QUARRY");
    }

    private static void assertHasItem(String addon, String id) {
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.addon().equals(addon) && item.id().equals(id)),
            "Missing dependency " + id + " for " + addon);
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
            .filter(item -> item.addon().equals("Crystamae Historia"))
            .count();

        assertEquals(54, count);
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.id().equals("CRY_CRYSTAL_COMMON_ELEMENTAL")));
        assertTrue(DependencyCatalog.items().stream()
            .anyMatch(item -> item.id().equals("CRY_CRYSTAL_UNIQUE_PHILOSOPHICAL")));
    }
}
