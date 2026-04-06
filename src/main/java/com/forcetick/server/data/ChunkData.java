package com.forcetick.server.data;

import net.minecraft.util.math.ChunkPos;

/**
 * Chunk data class - Based on FXNT Chunks ForceLoadedChunkData
 */
public class ChunkData {
    public String dimension;
    public ChunkPos chunkPos;

    public ChunkData(String dimension, ChunkPos chunkPos) {
        this.dimension = dimension;
        this.chunkPos = chunkPos;
    }

    /**
     * Generate chunk key - like FXNT generateChunkKey()
     * Format: dimension,x,z
     */
    public static String generateKey(String dimension, ChunkPos chunkPos) {
        return dimension + "," + chunkPos.x + "," + chunkPos.z;
    }

    /**
     * Parse chunk key - like FXNT getChunkData()
     */
    public static ChunkData fromKey(String chunkKey) {
        String[] parts = chunkKey.split(",");
        String dimension = parts[0];
        int x = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        return new ChunkData(dimension, new ChunkPos(x, z));
    }

    @Override
    public String toString() {
        return "ChunkData{" + dimension + ", " + chunkPos + "}";
    }
}
