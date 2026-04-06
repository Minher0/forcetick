package com.forcetick.server.manager;

import com.forcetick.server.ForceTickMod;
import com.forcetick.server.data.ChunkData;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * ForceTick Manager - Based on FXNT Chunks ChunkServer.java
 *
 * Manages forced chunks exactly like FXNT Chunks:
 * - Uses setChunkForced() to keep chunks loaded
 * - Applies manual random ticks for crop growth
 */
public class ForceTickManager {
    public static boolean INITIALIZED = false;
    public static Set<String> FORCE_LOADED_CHUNKS = new HashSet<>();
    public static boolean FORCE_LOADING_STATUS = true;

    /**
     * Update forced chunks - exactly like FXNT updateForceLoadedChunks()
     */
    public static void updateForceLoadedChunks(MinecraftServer server) {
        if (!INITIALIZED) return;

        // If force loading disabled, unload all chunks
        for (String chunkKey : FORCE_LOADED_CHUNKS) {
            ChunkData chunkData = ChunkData.fromKey(chunkKey);
            if (!FORCE_LOADING_STATUS) {
                unloadChunk(server, chunkData);
            } else {
                loadChunk(server, chunkData);
            }
        }
    }

    /**
     * Random tick forced chunks - EXACTLY like FXNT randomTickForceLoadedChunks()
     */
    public static void randomTickForceLoadedChunks(MinecraftServer server) {
        if (!INITIALIZED) return;
        if (ForceTickMod.randomTickSpeed == 0) return;

        for (String chunkKey : FORCE_LOADED_CHUNKS) {
            ChunkData chunkData = ChunkData.fromKey(chunkKey);

            ServerWorld level = null;
            for (ServerWorld serverLevel : server.getWorlds()) {
                if (serverLevel.getRegistryKey().getValue().toString().equals(chunkData.dimension)) {
                    level = serverLevel;
                    break;
                }
            }
            if (level == null) continue;

            WorldChunk chunk = level.getChunk(chunkData.chunkPos.x, chunkData.chunkPos.z);

            Profiler profiler = level.getProfiler();
            int startX = chunk.getPos().getStartX();
            int startZ = chunk.getPos().getStartZ();
            int yOffset = level.getBottomY();

            // Iterate through chunk sections - EXACTLY like FXNT
            for (ChunkSection chunkSection : chunk.getSectionArray()) {
                if (chunkSection == null || chunkSection.isEmpty()) {
                    yOffset += 16;
                    continue;
                }

                // Check if section has randomly ticking blocks - like FXNT isRandomlyTicking()
                boolean hasRandomTicking = hasRandomlyTickingBlocks(chunkSection);

                if (!hasRandomTicking) {
                    yOffset += 16;
                    continue;
                }

                // Apply randomTickSpeed random ticks per section - EXACTLY like FXNT
                Random random = level.random;
                for (int m = 0; m < ForceTickMod.randomTickSpeed; m++) {
                    // Get random position in chunk - EXACTLY like FXNT getBlockRandomPos
                    BlockPos randomPosInChunk = getBlockRandomPos(startX, yOffset, startZ, 15, random);

                    profiler.push("randomTick");

                    BlockState blockState = chunkSection.getBlockState(
                            randomPosInChunk.getX() - startX,
                            randomPosInChunk.getY() - yOffset,
                            randomPosInChunk.getZ() - startZ);

                    // Random tick block - EXACTLY like FXNT
                    if (blockState.hasRandomTicks()) {
                        blockState.randomTick(level, randomPosInChunk, random);
                    }

                    // Random tick fluid - EXACTLY like FXNT
                    FluidState fluidState = blockState.getFluidState();
                    if (fluidState.hasRandomTicks()) {
                        fluidState.onRandomTick(level, randomPosInChunk, random);
                    }

                    profiler.pop();
                }

                yOffset += 16;
            }
        }
    }

    /**
     * Check if section has randomly ticking blocks
     */
    private static boolean hasRandomlyTickingBlocks(ChunkSection section) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState state = section.getBlockState(x, y, z);
                    if (state.hasRandomTicks() || state.getFluidState().hasRandomTicks()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get random block position - like FXNT getBlockRandomPos
     */
    private static BlockPos getBlockRandomPos(int startX, int startY, int startZ, int range, Random random) {
        int x = startX + random.nextInt(range + 1);
        int y = startY + random.nextInt(range + 1);
        int z = startZ + random.nextInt(range + 1);
        return new BlockPos(x, y, z);
    }

    /**
     * Load chunk - exactly like FXNT loadChunk()
     */
    private static void loadChunk(ServerWorld level, ChunkPos chunkPos) {
        if (!isForceLoaded(level, chunkPos)) {
            ForceTickMod.LOGGER.info("Force Load Chunk: {} {}", level.getRegistryKey().getValue().toString(), chunkPos);
            level.setChunkForced(chunkPos.x, chunkPos.z, true);
        }
    }

    private static void loadChunk(MinecraftServer server, ChunkData chunkData) {
        if (!FORCE_LOADING_STATUS) return;

        for (ServerWorld level : server.getWorlds()) {
            if (level.getRegistryKey().getValue().toString().equals(chunkData.dimension)) {
                loadChunk(level, chunkData.chunkPos);
            }
        }
    }

    /**
     * Unload chunk - exactly like FXNT unloadChunk()
     */
    private static void unloadChunk(ServerWorld level, ChunkPos chunkPos) {
        if (isForceLoaded(level, chunkPos)) {
            ForceTickMod.LOGGER.info("Disable Force Loading on Chunk: {} {}", level.getRegistryKey().getValue().toString(), chunkPos);
            level.setChunkForced(chunkPos.x, chunkPos.z, false);
        }
    }

    private static void unloadChunk(MinecraftServer server, ChunkData chunkData) {
        for (ServerWorld level : server.getWorlds()) {
            if (level.getRegistryKey().getValue().toString().equals(chunkData.dimension)) {
                unloadChunk(level, chunkData.chunkPos);
            }
        }
    }

    /**
     * Check if chunk is force loaded - like FXNT isForceLoaded()
     */
    public static boolean isForceLoaded(ServerWorld level, ChunkPos chunkPos) {
        LongSet forceLoadedChunks = level.getForcedChunks();
        long chunkLong = ChunkPos.toLong(chunkPos.x, chunkPos.z);
        return forceLoadedChunks.contains(chunkLong);
    }

    /**
     * Add chunk - like FXNT addChunk()
     */
    public static void addChunk(ServerWorld level, ChunkPos chunkPos) {
        String dimension = level.getRegistryKey().getValue().toString();
        String chunkKey = ChunkData.generateKey(dimension, chunkPos);

        FORCE_LOADED_CHUNKS.add(chunkKey);
        loadChunk(level, chunkPos);
        saveForceLoadedChunks(level.getServer());

        ForceTickMod.LOGGER.info("Added force-tick chunk: {} at {}", chunkPos, dimension);
    }

    /**
     * Remove chunk - like FXNT removeChunk()
     */
    public static void removeChunk(ServerWorld level, ChunkPos chunkPos) {
        String dimension = level.getRegistryKey().getValue().toString();

        FORCE_LOADED_CHUNKS.removeIf(chunkKey -> {
            ChunkData data = ChunkData.fromKey(chunkKey);
            return data.dimension.equals(dimension) && data.chunkPos.equals(chunkPos);
        });

        unloadChunk(level, chunkPos);
        saveForceLoadedChunks(level.getServer());

        ForceTickMod.LOGGER.info("Removed force-tick chunk: {} at {}", chunkPos, dimension);
    }

    /**
     * Get all force-tick chunks for a dimension
     */
    public static List<ChunkPos> getChunksForDimension(String dimension) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (String chunkKey : FORCE_LOADED_CHUNKS) {
            ChunkData data = ChunkData.fromKey(chunkKey);
            if (data.dimension.equals(dimension)) {
                chunks.add(data.chunkPos);
            }
        }
        return chunks;
    }

    /**
     * Get total chunk count
     */
    public static int getTotalChunkCount() {
        return FORCE_LOADED_CHUNKS.size();
    }

    /**
     * Remove all chunks for a dimension
     */
    public static int removeAllChunks(ServerWorld level) {
        String dimension = level.getRegistryKey().getValue().toString();
        int count = 0;

        Iterator<String> iterator = FORCE_LOADED_CHUNKS.iterator();
        while (iterator.hasNext()) {
            String chunkKey = iterator.next();
            ChunkData data = ChunkData.fromKey(chunkKey);
            if (data.dimension.equals(dimension)) {
                unloadChunk(level, data.chunkPos);
                iterator.remove();
                count++;
            }
        }

        saveForceLoadedChunks(level.getServer());
        ForceTickMod.LOGGER.info("Removed all {} force-tick chunks from {}", count, dimension);
        return count;
    }

    /**
     * Save force loaded chunks - EXACTLY like FXNT saveForceLoadedChunks()
     */
    public static void saveForceLoadedChunks(MinecraftServer server) {
        StringBuilder stringBuilder = new StringBuilder();

        if (FORCE_LOADING_STATUS) {
            stringBuilder.append("status:enabled");
        } else {
            stringBuilder.append("status:disabled");
        }
        stringBuilder.append("\n");

        // Sort chunks
        List<String> sortedList = new ArrayList<>(FORCE_LOADED_CHUNKS);
        Collections.sort(sortedList);

        for (String chunkKey : sortedList) {
            stringBuilder.append(chunkKey).append("\n");
        }

        try (FileWriter fileWriter = new FileWriter(ForceTickMod.CHUNK_DATA_FILE_PATH)) {
            fileWriter.write(stringBuilder.toString());
        } catch (IOException e) {
            ForceTickMod.LOGGER.error("Error Saving Force Loaded Chunk Data: {}", e.toString());
        }
    }

    /**
     * Load force loaded chunks - EXACTLY like FXNT loadForceLoadedChunks()
     */
    public static void loadForceLoadedChunks() {
        ForceTickMod.LOGGER.info("Loading Force Loaded Chunk Data");
        FORCE_LOADED_CHUNKS.clear();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(ForceTickMod.CHUNK_DATA_FILE_PATH));

            String chunkKey = reader.readLine();
            while (chunkKey != null) {
                if (chunkKey.contains("status:")) {
                    String[] parts = chunkKey.split(":");
                    FORCE_LOADING_STATUS = parts[1].equals("enabled");
                } else {
                    if (!chunkKey.trim().isEmpty()) {
                        FORCE_LOADED_CHUNKS.add(chunkKey);
                    }
                }
                chunkKey = reader.readLine();
            }

            reader.close();
            INITIALIZED = true;

            ForceTickMod.LOGGER.info("Loaded {} force-tick chunks", FORCE_LOADED_CHUNKS.size());
        } catch (IOException e) {
            ForceTickMod.LOGGER.info("Force Loaded Chunk Data File Not Found - will create on save");
            INITIALIZED = true;
        }
    }
}
