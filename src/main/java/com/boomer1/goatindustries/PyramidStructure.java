package com.boomer1.goatindustries;

import org.bukkit.Material;
import org.bukkit.block.Block;

public final class PyramidStructure {

    private static final int[] LAYER_SIZES = {1, 3, 5, 7, 9};

    private PyramidStructure() {
    }

    public static int[] layerSizes() {
        return LAYER_SIZES.clone();
    }

    public static int requiredBlockCount() {
        int total = 0;
        for (int size : LAYER_SIZES) {
            total += size * size;
        }
        return total;
    }

    public static boolean isValidPyramid(Block topBlock) {
        if (topBlock == null || topBlock.getType() != Material.NETHERITE_BLOCK) {
            return false;
        }

        if (topBlock.getY() - (LAYER_SIZES.length - 1) < topBlock.getWorld().getMinHeight()) {
            return false;
        }

        int centerX = topBlock.getX();
        int centerZ = topBlock.getZ();

        for (int layer = 0; layer < LAYER_SIZES.length; layer++) {
            int size = LAYER_SIZES[layer];
            int radius = size / 2;
            int y = topBlock.getY() - layer;

            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (topBlock.getWorld().getBlockAt(centerX + xOffset, y, centerZ + zOffset)
                        .getType() != Material.NETHERITE_BLOCK) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
