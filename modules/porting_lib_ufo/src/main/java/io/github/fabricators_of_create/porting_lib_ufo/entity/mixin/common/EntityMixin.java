package io.github.fabricators_of_create.porting_lib_ufo.entity.mixin.common;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.fabricators_of_create.porting_lib_ufo.entity.IEntityAdditionalSpawnData;
import io.github.fabricators_of_create.porting_lib_ufo.entity.PortingLibEntity;
import io.github.fabricators_of_create.porting_lib_ufo.entity.events.EntityDataEvents;
import io.github.fabricators_of_create.porting_lib_ufo.entity.events.EntityEvents;
import io.github.fabricators_of_create.porting_lib_ufo.entity.events.EntityMountEvents;
import io.github.fabricators_of_create.porting_lib_ufo.entity.events.MinecartEvents;
import io.github.fabricators_of_create.porting_lib_ufo.entity.extensions.EntityExtensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtensions {
	@Unique
	private Collection<ItemEntity> port_lib$captureDrops = null;

	@Inject(at = @At("TAIL"), method = "<init>")
	public void port_lib$entityInit(EntityType<?> entityType, Level world, CallbackInfo ci) {
		try {
			if (((Entity)((Object)this)) instanceof LivingEntity living)
			{
				if (living.getAttributes() == null) {
					return;
				}
			}
			EntityDimensions dims = ((Entity) (Object) this).getDimensions(this.getPose());
			if (dims == null) {
				return;
			}
			float eye = dims.eyeHeight();
			dims.withEyeHeight(EntityEvents.EYE_HEIGHT.invoker().onEntitySize((Entity) (Object) this, eye));
		} catch (Exception e) {
			// Subclass fields may not be initialized yet during super constructor
			// (e.g. LivingEntity.getAttributes() == null from Origins / AdditionalEntityAttributes)
		}
	}

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityDimensions;eyeHeight()F"))
	private float entitySizeConstructEvent(EntityDimensions instance, Operation<Float> original) {
		try {
			EntityEvents.Size sizeEvent = new EntityEvents.Size((Entity) (Object) this, Pose.STANDING, this.dimensions, original.call(dimensions));
			sizeEvent.sendEvent();
			this.dimensions = sizeEvent.getNewSize();
			return sizeEvent.getNewEyeHeight();
		} catch (Exception e) {
			// Subclass fields may not be initialized yet during super constructor
			return original.call(instance);
		}
	}

	@WrapOperation(method = "refreshDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityDimensions;eyeHeight()F"))
	private float entitySizeEvent(EntityDimensions instance, Operation<Float> original, @Local(index = 3) EntityDimensions old, @Share("size") LocalRef<EntityEvents.Size> event) {
		EntityEvents.Size sizeEvent = new EntityEvents.Size((Entity) (Object) this, getPose(), this.dimensions, old, this.dimensions.eyeHeight(), original.call(dimensions));
		event.set(sizeEvent);
		sizeEvent.sendEvent();
		this.dimensions = sizeEvent.getNewSize();
		return sizeEvent.getNewEyeHeight();
	}

	@ModifyVariable(method = "refreshDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;reapplyPosition()V"), index = 3)
	private EntityDimensions modifyDimensions(EntityDimensions value, @Share("size") LocalRef<EntityEvents.Size> event) {
		return event.get().getNewSize();
	}

	// custom spawn packets

	@ModifyReturnValue(method = "getAddEntityPacket", at = @At("RETURN"))
	private Packet<ClientGamePacketListener> useExtendedSpawnPacket(Packet<ClientGamePacketListener> base) {
		if (!(this instanceof IEntityAdditionalSpawnData))
			return base;
		PortingLibEntity.LOGGER.warn(getClass().getSimpleName() + " is using IEntityAdditionalSpawnData without a custom packet. Please migrate to using PortingLibEntity.getEntitySpawningPacket. This functionality will be removed in 1.20.4!");
		return PortingLibEntity.getEntitySpawningPacket((Entity) (Object) this, base);
	}

	// CAPTURE DROPS

	@WrapWithCondition(
			method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
			)
	)
	public boolean port_lib$captureDrops(Level level, Entity entity) {
		if (port_lib$captureDrops != null && entity instanceof ItemEntity item) {
			port_lib$captureDrops.add(item);
			return false;
		}
		return true;
	}

	@Unique
	@Override
	public Collection<ItemEntity> captureDrops() {
		return port_lib$captureDrops;
	}

	@Unique
	@Override
	public Collection<ItemEntity> captureDrops(Collection<ItemEntity> value) {
		Collection<ItemEntity> ret = port_lib$captureDrops;
		port_lib$captureDrops = value;
		return ret;
	}

	@Shadow
	@Nullable
	private Entity vehicle;

	@Shadow
	private Level level;

	@Shadow
	public abstract void unRide();

	@Shadow
	protected abstract void removeAfterChangingDimensions();

	@Shadow
	public abstract boolean isRemoved();

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	private float yRot;

	@Shadow
	public abstract int getId();

	@Shadow
	public abstract Pose getPose();

	@Shadow
	private EntityDimensions dimensions;

	@Inject(
			method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z",
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	public void port_lib$startRiding(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
		if (!EntityMountEvents.MOUNT.invoker().canStartRiding(entity, (Entity) (Object) this)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "removeVehicle", at = @At(value = "CONSTANT", args = "nullValue=true"), cancellable = true)
	public void port_lib$removeRidingEntity(CallbackInfo ci) {
		if (!EntityMountEvents.DISMOUNT.invoker().onStopRiding(this.vehicle, (Entity) (Object) this)) {
			ci.cancel();
		}
	}

	@Inject(method = "remove", at = @At("TAIL"))
	public void port_lib$onEntityRemove(Entity.RemovalReason reason, CallbackInfo ci) {
		EntityEvents.ON_REMOVE.invoker().onRemove((Entity) (Object) this, reason);
		if ((Object) this instanceof AbstractMinecart cart) {
			MinecartEvents.REMOVE.invoker().minecartRemove(cart, level);
		}
	}

	// custom data

	@Unique
	private CompoundTag customData;

	@Inject(
			method = "saveWithoutId",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
			)
	)
	private void saveCustomData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (customData != null && !customData.isEmpty()) {
			tag.put("ForgeData", customData);
		}
	}

	@Inject(
			method = "load",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
			)
	)
	private void loadCustomData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("ForgeData")) {
			customData = tag.getCompound("ForgeData");
		}
	}

	@Override
	public CompoundTag getCustomData() {
		if (customData == null)
			customData = new CompoundTag();
		return customData;
	}

	// data events

	@Inject(method = "saveWithoutId", at = @At("RETURN"))
	public void afterSave(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
		EntityDataEvents.SAVE.invoker().onSave((Entity) (Object) this, nbt);
	}

	@Inject(method = "load", at = @At("RETURN"))
	public void afterLoad(CompoundTag nbt, CallbackInfo ci) {
		EntityDataEvents.LOAD.invoker().onLoad((Entity) (Object) this, nbt);
	}
}
