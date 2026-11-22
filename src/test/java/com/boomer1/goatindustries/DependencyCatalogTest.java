package com.boomer1.goatindustries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DependencyCatalogTest {

    @Test
    void catalogContainsAllCollectedRequirementsWithoutDuplicateIds() {
        assertEquals(137, DependencyCatalog.items().size());

        Set<String> ids = new HashSet<>();
        DependencyCatalog.items().forEach(item -> assertTrue(ids.add(item.id()),
            "Duplicate dependency ID: " + item.id()));
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
