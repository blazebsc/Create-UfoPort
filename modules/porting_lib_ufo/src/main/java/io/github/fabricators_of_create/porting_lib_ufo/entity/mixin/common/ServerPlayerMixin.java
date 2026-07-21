package io.github.fabricators_of_create.porting_lib_ufo.entity.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import io.github.fabricators_of_create.porting_lib_ufo.entity.EntityHooks;
import io.github.fabricators_of_create.porting_lib_ufo.entity.events.ServerPlayerCreationCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Shadow
	@Final
	public MinecraftServer server;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(MinecraftServer server, ServerLevel world, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
		try {
			ServerPlayerCreationCallback.EVENT.invoker().onCreate((ServerPlayer) (Object) this);
		} catch (Exception e) {
			// Subclass fields may not be initialized yet during super constructor
		}
	}

	@Inject(method = "restoreFrom", at = @At("TAIL"))
	private void copyPersistentData(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
		CompoundTag oldData = oldPlayer.getCustomData();
		CompoundTag persistent = oldData.getCompound("PlayerPersisted");
		if (persistent != null) {
			CompoundTag thisData = this.getCustomData();
			thisData.put("PlayerPersisted", persistent);
		}
	}

	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER), cancellable = true)
	private void onPlayerDie(DamageSource cause, CallbackInfo ci) {
		if (EntityHooks.onLivingDeath(this, cause))
			ci.cancel();
	}
}
