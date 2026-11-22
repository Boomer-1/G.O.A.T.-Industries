package com.boomer1.goatindustries;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PyramidStructureTest {

    @Test
    void pyramidLayersAreCorrectFromTopToBottom() {
        assertArrayEquals(new int[] {1, 3, 5, 7, 9}, PyramidStructure.layerSizes());
    }

    @Test
    void pyramidRequiresExactlyOneHundredSixtyFiveNetheriteBlocks() {
        assertEquals(165, PyramidStructure.requiredBlockCount());
    }

    @Test
    void layerSizesAreReturnedAsADefensiveCopy() {
        int[] sizes = PyramidStructure.layerSizes();
        sizes[0] = 99;

        assertArrayEquals(new int[] {1, 3, 5, 7, 9}, PyramidStructure.layerSizes());
    }
}
