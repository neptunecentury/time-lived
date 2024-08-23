package com.neptunecentury;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TimeLived implements ModInitializer {

    @Override
    public void onInitialize() {

        ServerLivingEntityEvents.AFTER_DEATH.register(((entity, damageSource) -> {
            if (entity instanceof PlayerEntity player) {
                var server = player.getServer();
                if (server == null) {
                    return;
                }

                var world = server.getOverworld();
                if (world != null) {
                    // Get the last time the player died

                    var key = "playerLastDeathTime";
                    var data = new NbtCompound();
                    player.getEntityData
                    player.readCustomDataFromNbt(data);

                    long lastTimePlayerDied = 0;
                    if (data.contains(key)){
                        lastTimePlayerDied = data.getLong(key);
                    }


                    var worldTime = world.getTime();

                    data.putLong(key, worldTime);

                    player.writeCustomDataToNbt(data);
                    player.sendMessage(Text.literal("You last died at [%s]. You lived for [%s]".formatted(worldTime, worldTime - lastTimePlayerDied)));
                }


            }
        }));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            var oldDeathTime = oldPlayer.deathTime;
            var tickTime = oldPlayer.server.getTickTime();


            // Calculate how long the player lived based on ticks
            // Send message to player
            newPlayer.sendMessage(Text.literal("Welcome Back. You last died at [%s]. Tick time [%s]".formatted(oldDeathTime, tickTime)));
        });
    }
}
