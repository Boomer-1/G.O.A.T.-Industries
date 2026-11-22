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
        assertEquals(138, DependencyCatalog.items().size());

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
