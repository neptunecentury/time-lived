package com.neptunecentury.timelived.mixin;

import com.neptunecentury.timelived.PlayerDeathData;
import com.neptunecentury.timelived.TimeLived;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerMixin {

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
        ServerPlayerEntity thisObject = (ServerPlayerEntity) (Object) this;

        // Get the time the player died
        var timePlayerJustDied = world.getTimeOfDay();

        // Get the existing player death data. If it does not exist in the hashmap, create a new instance. Since
        // this could be the first time the player joined the world, set their last death time to the time
        // they joined so their time lived isn't skewed.
        var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(thisObject.getUuid(), null);
        if (playerDeathData == null) {
            return;
        }

        // Update last time player died in the hashmap
        playerDeathData.timePlayerJustDied = timePlayerJustDied;

    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    private void mixinReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity thisObject = (ServerPlayerEntity) (Object) this;
        // Get the time the player last died from the nbt tag
        var playerDeathNbtData = nbt.getCompound(TimeLived.TIME_LIVED_PLAYER_DEATH_DATA);
        var playerDeathData = new PlayerDeathData();
        playerDeathData.timePlayerLastDied = playerDeathNbtData.getLong(TimeLived.TIME_PLAYER_LAST_DIED);
        playerDeathData.timePlayerJustDied = playerDeathNbtData.getLong(TimeLived.TIME_PLAYER_JUST_DIED);
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

        var playerDeathNbtData = new NbtCompound();
        playerDeathNbtData.putLong(TimeLived.TIME_PLAYER_LAST_DIED, playerDeathData.timePlayerLastDied);
        playerDeathNbtData.putLong(TimeLived.TIME_PLAYER_JUST_DIED, playerDeathData.timePlayerJustDied);
        // Put the player death data nbt compound into the custom data nbt compound.
        nbt.put(TimeLived.TIME_LIVED_PLAYER_DEATH_DATA, playerDeathNbtData);

    }

}

