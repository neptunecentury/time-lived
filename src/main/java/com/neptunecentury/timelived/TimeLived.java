package com.neptunecentury.timelived;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class TimeLived implements ModInitializer {
    public static final HashMap<UUID, PlayerDeathData> playerDeathDataHash = new HashMap<>();
    //private static final String keyId = "time_since_last_death";
    //public static final Identifier TIME_SINCE_LAST_DEATH = new Identifier("time-lived", keyId);
    public static MinecraftServer _server;
    public static final String TIME_LIVED_PLAYER_DEATH_DATA = "TimeLivedPlayerDeathData";
    public static final String TIME_PLAYER_LAST_DIED = "TimePlayerLastDied";
    public static final String TIME_PLAYER_JUST_DIED = "TimePlayerJustDied";

    @Override
    public void onInitialize() {

        // Register event when player joins server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // A player has joined the server.
            // Set the server instance
            _server = server;
            var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(handler.player.getUuid(), null);
            if (playerDeathData == null) {
                playerDeathData = new PlayerDeathData();
                // Get the time of day the player joined if there is no death data because this
                // could be the first time the player joined.
                playerDeathData.timePlayerLastDied = server.getOverworld().getTimeOfDay();
                // Create new instance and save it in the hashmap
                TimeLived.playerDeathDataHash.put(handler.player.getUuid(), playerDeathData);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            // When the player leaves, remove the data from the hashmap.
            //playerDeathDataHash.remove(handler.player.getUuid());
        }));

        //ServerPlayerEvents.COPY_FROM.register(((oldPlayer, newPlayer, alive) -> {
        //}));

        // Register when player respawns
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Once the player respawns, calculate how long the player live for by taking the last
            // time of death, how long they were dead for, and the current world time.
            // First, get the player death data from the hashmap if it exists
            var playerDeathData = TimeLived.playerDeathDataHash.get(oldPlayer.getUuid() );
            if (playerDeathData == null){
                return;
            }

            // Get the current world
            var world = oldPlayer.getServerWorld();
            if (world == null) {
               return;
            }

            // Calculate how long the player lived.
            var timeLived = playerDeathData.timePlayerJustDied - playerDeathData.timePlayerLastDied;
            // Since the world is still ticking while the player is dead, update the last time of death
            // after the player has respawned.
            playerDeathData.timePlayerLastDied = world.getTimeOfDay();

            // Send message to the player
            DecimalFormat df = new DecimalFormat("#.##");
            var daysLived = (double) timeLived / 24000;
            newPlayer.sendMessage(Text.literal("Congrats... you lived for [%s] day(s).".formatted(df.format(daysLived))));
        });
    }
}
