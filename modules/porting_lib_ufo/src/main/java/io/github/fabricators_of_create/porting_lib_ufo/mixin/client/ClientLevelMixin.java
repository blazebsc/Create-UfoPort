package io.github.fabricators_of_create.porting_lib_ufo.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fabricators_of_create.porting_lib_ufo.common.util.MixinHelper;
import io.github.fabricators_of_create.porting_lib_ufo.event.client.ClientWorldEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Environment(EnvType.CLIENT)
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void port_lib$init(CallbackInfo ci) {
		try {
			ClientWorldEvents.LOAD.invoker().onWorldLoad(minecraft, MixinHelper.cast(this));
		} catch (Exception e) {
			// Subclass fields may not be initialized yet during super constructor
		}
	}
}
