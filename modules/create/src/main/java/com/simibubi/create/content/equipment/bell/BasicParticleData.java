package com.simibubi.create.content.equipment.bell;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BasicParticleData<T extends Particle> implements ParticleOptions, ICustomParticleDataWithSprite<BasicParticleData<T>> {

	public BasicParticleData() { }

	@Override
	public MapCodec<BasicParticleData<T>> getCodec(ParticleType<BasicParticleData<T>> type) {
		return MapCodec.unit(this);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BasicParticleData<T>> getStreamCodec(
			ParticleType<BasicParticleData<T>> type) {
		return StreamCodec.unit(this);
	}

	@Override
	public ParticleType<BasicParticleData<T>> createType() {
		BasicParticleData<T> self = this;
		return new ParticleType<BasicParticleData<T>>(false) {
			@Override
			public MapCodec<BasicParticleData<T>> codec() {
				return self.getCodec(this);
			}
			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, BasicParticleData<T>> streamCodec() {
				return self.getStreamCodec(this);
			}
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ParticleProvider<BasicParticleData<T>> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}

	public interface IBasicParticleFactory<U extends Particle> {
		U makeParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprite);
	}

	@Environment(EnvType.CLIENT)
	public abstract IBasicParticleFactory<T> getBasicFactory();

	@Override
	@Environment(EnvType.CLIENT)
	public ParticleEngine.SpriteParticleRegistration<BasicParticleData<T>> getMetaFactory() {
		return animatedSprite -> (data, worldIn, x, y, z, vx, vy, vz) ->
				getBasicFactory().makeParticle(worldIn, x, y, z, vx, vy, vz, animatedSprite);
	}

}
