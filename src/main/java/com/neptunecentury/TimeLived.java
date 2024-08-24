package com.neptunecentury;

import com.neptunecentury.timelived.mixin.PlayerMixin;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class TimeLived implements ModInitializer {
    public static final HashMap<UUID, Long> timePlayerLastDied = new HashMap<>();
    //private static final String keyId = "time_since_last_death";
    //public static final Identifier TIME_SINCE_LAST_DEATH = new Identifier("time-lived", keyId);
    public static MinecraftServer _server;
    public static final String TIME_LIVED_LAST_DEATH_TIME = "TimeLivedLastDeathTime";

    @Override
    public void onInitialize() {

        // Register event when player joins server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // A player has joined the server.
            // Set the server instance
            _server = server;

        });

        ServerPlayerEvents.COPY_FROM.register(((oldPlayer, newPlayer, alive) -> {
            var nbt = new NbtCompound();
            oldPlayer.writeCustomDataToNbt(nbt);
            // Get the value we need from the tag
            var timePlayerLastDied = nbt.getLong(TIME_LIVED_LAST_DEATH_TIME);

            // Now, set the tag to the new player
            var newNbt = new NbtCompound();
            newPlayer.writeCustomDataToNbt(newNbt);

            newNbt.putLong(TIME_LIVED_LAST_DEATH_TIME, timePlayerLastDied);
            // Read the nbt into the new player
            newPlayer.readCustomDataFromNbt(newNbt);


        }));
        //Registry.register(Registries.CUSTOM_STAT, keyId, TIME_SINCE_LAST_DEATH);
        //var customStat = Stats.CUSTOM.getOrCreateStat(TIME_SINCE_LAST_DEATH, StatFormatter.TIME);

//        ServerLivingEntityEvents.AFTER_DEATH.register(((entity, damageSource) -> {
//            if (entity instanceof PlayerEntity player) {
//                var server = player.getServer();
//                if (server == null) {
//                    return;
//                }
//
//                var world = server.getOverworld();
//                if (world != null) {
//                    // Get the last time the player died
//
//
//                    var key = "playerLastDeathTime";
//                    var data = new NbtCompound();
//
//                    player.writeCustomDataToNbt(data);
//
//                    long lastTimePlayerDied = 0;
//                    if (data.contains(key)) {
//                        lastTimePlayerDied = data.getLong(key);
//                    }
//
//
//                    var worldTime = world.getTime();
//
//                    data.putLong(key, worldTime);
//
//                    player.readCustomDataFromNbt(data);
//                    player.saveNbt(data);
//                    player.sendMessage(Text.literal("You last died at [%s]. You lived for [%s]".formatted(worldTime, worldTime - lastTimePlayerDied)));
//                }
//
//
//            }
//        }));
//
//        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
//            var oldDeathTime = oldPlayer.deathTime;
//            var tickTime = oldPlayer.server.getTickTime();
//
//
//            // Calculate how long the player lived based on ticks
//            // Send message to player
//            newPlayer.sendMessage(Text.literal("Welcome Back. You last died at [%s]. Tick time [%s]".formatted(oldDeathTime, tickTime)));
//        });
    }
}
