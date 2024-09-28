package com.neptunecentury.timelived.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(at = @At("HEAD"), method = "")

}
