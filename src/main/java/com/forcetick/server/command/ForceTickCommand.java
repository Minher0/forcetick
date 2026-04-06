package com.forcetick.server.command;

import com.forcetick.server.manager.ForceTickManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * ForceTick Command - Based on FXNT Chunks approach
 */
public class ForceTickCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("forcetick")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("add")
                .executes(ForceTickCommand::addCurrentChunk)
                .then(CommandManager.argument("x", IntegerArgumentType.integer())
                    .then(CommandManager.argument("z", IntegerArgumentType.integer())
                        .executes(ForceTickCommand::addSpecificChunk))))
            .then(CommandManager.literal("remove")
                .executes(ForceTickCommand::removeCurrentChunk)
                .then(CommandManager.argument("x", IntegerArgumentType.integer())
                    .then(CommandManager.argument("z", IntegerArgumentType.integer())
                        .executes(ForceTickCommand::removeSpecificChunk))))
            .then(CommandManager.literal("removeall")
                .executes(ForceTickCommand::removeAllChunks))
            .then(CommandManager.literal("list")
                .executes(ForceTickCommand::listChunks))
        );
    }

    private static int addCurrentChunk(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        Vec3d pos = source.getPosition();
        BlockPos blockPos = BlockPos.ofFloored(pos);
        ChunkPos chunkPos = new ChunkPos(blockPos);

        return addChunk(source, world, chunkPos);
    }

    private static int addSpecificChunk(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        int x = IntegerArgumentType.getInteger(context, "x");
        int z = IntegerArgumentType.getInteger(context, "z");
        ChunkPos chunkPos = new ChunkPos(x, z);

        return addChunk(source, world, chunkPos);
    }

    private static int addChunk(ServerCommandSource source, ServerWorld world, ChunkPos chunkPos) {
        String dimension = world.getRegistryKey().getValue().toString();

        // Check if already exists
        for (String chunkKey : ForceTickManager.FORCE_LOADED_CHUNKS) {
            String[] parts = chunkKey.split(",");
            if (parts[0].equals(dimension) &&
                Integer.parseInt(parts[1]) == chunkPos.x &&
                Integer.parseInt(parts[2]) == chunkPos.z) {
                source.sendFeedback(() -> Text.literal("§e[ForceTick] Ce chunk est déjà forcé: " +
                    chunkPos.x + ", " + chunkPos.z), false);
                return 0;
            }
        }

        ForceTickManager.addChunk(world, chunkPos);

        source.sendFeedback(() -> Text.literal("§a[ForceTick] Chunk ajouté: " +
            chunkPos.x + ", " + chunkPos.z + " dans " + dimension), true);
        return 1;
    }

    private static int removeCurrentChunk(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        Vec3d pos = source.getPosition();
        BlockPos blockPos = BlockPos.ofFloored(pos);
        ChunkPos chunkPos = new ChunkPos(blockPos);

        return removeChunk(source, world, chunkPos);
    }

    private static int removeSpecificChunk(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        int x = IntegerArgumentType.getInteger(context, "x");
        int z = IntegerArgumentType.getInteger(context, "z");
        ChunkPos chunkPos = new ChunkPos(x, z);

        return removeChunk(source, world, chunkPos);
    }

    private static int removeChunk(ServerCommandSource source, ServerWorld world, ChunkPos chunkPos) {
        String dimension = world.getRegistryKey().getValue().toString();

        // Check if exists
        boolean exists = false;
        for (String chunkKey : ForceTickManager.FORCE_LOADED_CHUNKS) {
            String[] parts = chunkKey.split(",");
            if (parts[0].equals(dimension) &&
                Integer.parseInt(parts[1]) == chunkPos.x &&
                Integer.parseInt(parts[2]) == chunkPos.z) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            source.sendFeedback(() -> Text.literal("§e[ForceTick] Ce chunk n'était pas forcé: " +
                chunkPos.x + ", " + chunkPos.z), false);
            return 0;
        }

        ForceTickManager.removeChunk(world, chunkPos);

        source.sendFeedback(() -> Text.literal("§c[ForceTick] Chunk retiré: " +
            chunkPos.x + ", " + chunkPos.z), true);
        return 1;
    }

    private static int removeAllChunks(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        int count = ForceTickManager.removeAllChunks(world);

        source.sendFeedback(() -> Text.literal("§c[ForceTick] " + count +
            " chunks retirés de " + world.getRegistryKey().getValue()), true);
        return count;
    }

    private static int listChunks(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        String dimension = world.getRegistryKey().getValue().toString();

        List<ChunkPos> chunks = ForceTickManager.getChunksForDimension(dimension);

        if (chunks.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§e[ForceTick] Aucun chunk forcé dans " +
                dimension), false);
        } else {
            source.sendFeedback(() -> Text.literal("§a[ForceTick] " + chunks.size() +
                " chunks forcés dans " + dimension + ":"), false);

            for (ChunkPos pos : chunks) {
                final ChunkPos p = pos;
                source.sendFeedback(() -> Text.literal("§7  - Chunk: " + p.x + ", " + p.z), false);
            }
        }

        return chunks.size();
    }
}
