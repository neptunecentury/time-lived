package com.neptunecentury.timelived.mixin;

import com.neptunecentury.timelived.PlayerDeathData;
import com.neptunecentury.timelived.TimeLived;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(at = @At("HEAD"), method = "die")
    private void mixinOnDeath(DamageSource damageSource, CallbackInfo ci) {
        var server = TimeLived._server;
        if (server == null) {
            return;
        }
        var world = server.overworld();
        if (world == null) {
            return;
        }

        // Get the player object
        var thisPlayer = (ServerPlayer) (Object) this;

        // Get the time the player died
        var timePlayerJustDied = world.getOverworldClockTime();

        // Get the existing player death data. If it does not exist in the hashmap, create a new instance. Since
        // this could be the first time the player joined the world, set their last death time to the time
        // they joined so their time lived isn't skewed.
        var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(thisPlayer.getUUID(), null);
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
                var playerManager = server.getPlayerList();
                var players = playerManager.getPlayers();
                // Loop through each player and send a message, alerting everyone of the dead player's death.
                for (var player : players) {
                    if (player != thisPlayer) {
                        // Send player a message
                        player.sendSystemMessage(msg.withStyle(ChatFormatting.GREEN));
                    }
                }

            }
        }

    }

    @Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
    private void mixinReadCustomDataFromNbt(ValueInput view, CallbackInfo ci) {

        ServerPlayer thisObject = (ServerPlayer) (Object) this;
        // Get the time the player last died from the nbt tag
        //var playerDeathNbtData = nbt.getCompound(TimeLived.TIME_LIVED_PLAYER_DEATH_DATA);
        var playerDeathNbtData = view.child(TimeLived.TIME_LIVED_PLAYER_DEATH_DATA);
        var playerDeathData = new PlayerDeathData();

        if (playerDeathNbtData.isPresent()){
            var nbtData = playerDeathNbtData.get();
            // Read data
            playerDeathData.timePlayerLastDied = nbtData.getLongOr(TimeLived.TIME_PLAYER_LAST_DIED, 0);
            playerDeathData.timePlayerJustDied = nbtData.getLongOr(TimeLived.TIME_PLAYER_JUST_DIED, 0);
            playerDeathData.longestTimeLived = nbtData.getLongOr(TimeLived.LONGEST_TIME_LIVED, 0);
        }

        // Update last time player died in the hashmap
        TimeLived.playerDeathDataHash.put(thisObject.getUUID(), playerDeathData);
    }

    @Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
    private void mixinWriteCustomDataToNbt(ValueOutput view, CallbackInfo ci) {
        ServerPlayer thisObject = (ServerPlayer) (Object) this;
        // Get the time the player last died from the hashmap
        var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(thisObject.getUUID(), null);
        if (playerDeathData == null) {
            return;
        }

        // Create compound to store our custom data
        var playerDeathNbtData = new CompoundTag();
        playerDeathNbtData.putLong(TimeLived.TIME_PLAYER_LAST_DIED, playerDeathData.timePlayerLastDied);
        playerDeathNbtData.putLong(TimeLived.TIME_PLAYER_JUST_DIED, playerDeathData.timePlayerJustDied);
        playerDeathNbtData.putLong(TimeLived.LONGEST_TIME_LIVED, playerDeathData.longestTimeLived);
        // Put the player death data nbt compound into the custom data nbt compound.
        view.store(TimeLived.TIME_LIVED_PLAYER_DEATH_DATA, CompoundTag.CODEC, playerDeathNbtData);

    }

}

