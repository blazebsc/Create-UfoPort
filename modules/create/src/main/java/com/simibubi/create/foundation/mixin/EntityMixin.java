package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;

import net.minecraft.world.entity.Entity;

@Mixin(value = Entity.class, priority = 900)
public class EntityMixin {

	@Inject(method = "fireImmune()Z", at = @At("RETURN"), cancellable = true)
	public void create$onFireImmune(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			Entity self = (Entity) (Object) this;
			boolean immune = self.getCustomData().getBoolean(NetheriteDivingHandler.FIRE_IMMUNE_KEY);
			if (immune)
				cir.setReturnValue(immune);
		}
	}

	@Inject(method = "isPickable()Z", at = @At("RETURN"), cancellable = true)
	public void create$seatedPokemonClickThrough(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ())
			return;
		Entity self = (Entity) (Object) this;
		if (!(self.getVehicle() instanceof SeatEntity))
			return;
		if (!Mods.COBBLEMON.isLoaded())
			return;
		try {
			Class<?> pokemonEntityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
			if (pokemonEntityClass.isInstance(self))
				cir.setReturnValue(false);
		} catch (ClassNotFoundException ignored) {
		}
	}

}
