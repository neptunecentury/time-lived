package com.neptunecentury.timelived.mixin;

import com.neptunecentury.TimeLived;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerMixin {
    @Unique
    private final String lastDeathTimeKey = "TimeLivedLastDeathTime";

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void mixinOnDeath(DamageSource damageSource, CallbackInfo ci) {
        var server = TimeLived._server;
        if (server != null) {
            var world = server.getOverworld();
            if (world != null) {
                // Send a message to the player congratulating them... lol
                ServerPlayerEntity thisObject = (ServerPlayerEntity) (Object) this;
                // Get the last time the player died
                var timePlayerJustDied = world.getTime();
                // Get the last time the player died from the hashmap. If the player never died before, then this will
                // be 0. However, this is incorrect because the player may have never joined the server before.
                // Need to figure out a way to store when the player first joined.
                var timePlayerLastDied = TimeLived.timePlayerLastDied.getOrDefault(thisObject.getUuid(), 0L);

                // Calculate the difference between the time the player last died and the current time of death.
                var timeLived = timePlayerJustDied - timePlayerLastDied;

                // Update last time player died in the hashmap
                TimeLived.timePlayerLastDied.put(thisObject.getUuid(), timePlayerJustDied);


                thisObject.sendMessage(Text.literal("Congrats... you lived for [%s]".formatted(timeLived)));
            }
        }

    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    private void mixinReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity thisObject = (ServerPlayerEntity) (Object) this;
        // Get the time the player last died from the nbt tag
        var timePlayerLastDied = nbt.getLong(lastDeathTimeKey);
        // Update last time player died in the hashmap
        TimeLived.timePlayerLastDied.put(thisObject.getUuid(), timePlayerLastDied);
    }

    @Inject(at = @At("HEAD"), method = "writeCustomDataToNbt")
    private void mixinWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity thisObject = (ServerPlayerEntity) (Object) this;
        // Get the time the player last died from the hashmap
        var timePlayerLastDied = TimeLived.timePlayerLastDied.getOrDefault(thisObject.getUuid(), 0L);
        nbt.putLong(lastDeathTimeKey, timePlayerLastDied);

    }

}

