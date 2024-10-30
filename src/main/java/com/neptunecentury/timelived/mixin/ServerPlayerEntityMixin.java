package com.neptunecentury.timelived.mixin;

import com.neptunecentury.timelived.PlayerDeathData;
import com.neptunecentury.timelived.TimeLived;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void mixinOnDeath(DamageSource damageSource, CallbackInfo ci) {
        var server = TimeLived._server;
        if (server == null) {
            return;
        }
        var world = server.getOverworld();
        if (world == null) {
            return;
        }

        // Get the player object
        var thisPlayer = (ServerPlayerEntity) (Object) this;

        // Get the time the player died
        var timePlayerJustDied = world.getTimeOfDay();

        // Get the existing player death data. If it does not exist in the hashmap, create a new instance. Since
        // this could be the first time the player joined the world, set their last death time to the time
        // they joined so their time lived isn't skewed.
        var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(thisPlayer.getUuid(), null);
        if (playerDeathData == null) {
            return;
        }

        // Update last time player died in the hashmap
        playerDeathData.timePlayerJustDied = timePlayerJustDied;
        // Calculate how long the player lived.
        var timeLived = playerDeathData.timePlayerJustDied - playerDeathData.timePlayerLastDied;
        // Get how many days the player lived
        var daysLived = TimeLived.getDaysLived(timeLived);
        var previousDaysLived = TimeLived.getDaysLived(playerDeathData.longestTimeLived);
        // Get loaded config
        var cfg = TimeLived.get_cfg();

        // Broadcast messages to other players on the server
        if (cfg.enableMessagesToOthers && !cfg.timeLivedMessagesToOthers.isEmpty()) {
            // Get message to send
            var msg = TimeLived.getTimeLivedMessage(cfg.timeLivedMessagesToOthers, daysLived, previousDaysLived, thisPlayer);
            if (msg != null) {
                // Get the players on the server
                var playerManager = server.getPlayerManager();
                var players = playerManager.getPlayerList();
                // Loop through each player and send a message, alerting everyone of the dead player's death.
                for (var player : players) {
                    if (player != thisPlayer) {
                        // Send player a message
                        player.sendMessage(msg.formatted(Formatting.GREEN));
                    }
                }

            }
        }

    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    private void mixinReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity thisObject = (ServerPlayerEntity) (Object) this;
        // Get the time the player last died from the nbt tag
        var playerDeathNbtData = nbt.getCompound(TimeLived.TIME_LIVED_PLAYER_DEATH_DATA);
        var playerDeathData = new PlayerDeathData();
        playerDeathData.timePlayerLastDied = playerDeathNbtData.getLong(TimeLived.TIME_PLAYER_LAST_DIED);
        playerDeathData.timePlayerJustDied = playerDeathNbtData.getLong(TimeLived.TIME_PLAYER_JUST_DIED);
        playerDeathData.longestTimeLived = playerDeathNbtData.getLong(TimeLived.LONGEST_TIME_LIVED);
        // Update last time player died in the hashmap
        TimeLived.playerDeathDataHash.put(thisObject.getUuid(), playerDeathData);
    }

    @Inject(at = @At("HEAD"), method = "writeCustomDataToNbt")
    private void mixinWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity thisObject = (ServerPlayerEntity) (Object) this;
        // Get the time the player last died from the hashmap
        var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(thisObject.getUuid(), null);
        if (playerDeathData == null) {
            return;
        }

        // Create compound to store our custom data
        var playerDeathNbtData = new NbtCompound();
        playerDeathNbtData.putLong(TimeLived.TIME_PLAYER_LAST_DIED, playerDeathData.timePlayerLastDied);
        playerDeathNbtData.putLong(TimeLived.TIME_PLAYER_JUST_DIED, playerDeathData.timePlayerJustDied);
        playerDeathNbtData.putLong(TimeLived.LONGEST_TIME_LIVED, playerDeathData.longestTimeLived);
        // Put the player death data nbt compound into the custom data nbt compound.
        nbt.put(TimeLived.TIME_LIVED_PLAYER_DEATH_DATA, playerDeathNbtData);

    }

}

