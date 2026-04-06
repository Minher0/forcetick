package com.forcetick.server;

import com.forcetick.server.command.ForceTickCommand;
import com.forcetick.server.manager.ForceTickManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * ForceTick Server Mod
 *
 * Implementation based on FXNT Chunks by foxynotail.
 * Source: https://github.com/foxynotail/fxnt-chunks-mod-fabric
 *
 * Force Tick = forced chunk loading + manual random ticks for crop growth
 * The game naturally handles: entities, block entities, scheduled ticks
 */
public class ForceTickMod implements ModInitializer {
    public static final String MOD_ID = "forcetick-server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static int serverTick = 0;
    public static int randomTickSpeed = 3;
    public static String CHUNK_DATA_FILE = MOD_ID + "-chunks.txt";
    public static File CHUNK_DATA_FILE_PATH = new File(CHUNK_DATA_FILE);

    @Override
    public void onInitialize() {
        LOGGER.info("ForceTick Server Mod initializing...");
        LOGGER.info("Based on FXNT Chunks concept by foxynotail");

        // World load event - set paths and load data (like FXNT)
        ServerWorldEvents.LOAD.register((server, world) -> {
            // Set file path to world folder
            File runDir = server.getRunDirectory();
            CHUNK_DATA_FILE_PATH = new File(runDir, "world/" + CHUNK_DATA_FILE);

            // Load chunk data
            ForceTickManager.loadForceLoadedChunks();
        });

        // World unload event - save data (like FXNT)
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            ForceTickManager.saveForceLoadedChunks(server);
        });

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ForceTickCommand.register(dispatcher);
        });

        // Server tick event - exactly like FXNT (every tick)
        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            // Get randomTickSpeed from GameRules (like FXNT)
            randomTickSpeed = server.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);

            // Run random ticks on forced chunks (like FXNT)
            ForceTickManager.randomTickForceLoadedChunks(server);

            // Run every second (like FXNT)
            if (serverTick % 20 == 0) {
                ForceTickManager.updateForceLoadedChunks(server);
            }
            if (serverTick >= 20) {
                serverTick = 0;
            } else {
                serverTick++;
            }
        });

        LOGGER.info("ForceTick Server Mod initialized!");
    }
}
