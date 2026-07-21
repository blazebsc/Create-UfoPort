package com.simibubi.create.content.kinetics.steamEngine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class SteamJetParticleData implements ParticleOptions, ICustomParticleDataWithSprite<SteamJetParticleData> {

	public static final MapCodec<SteamJetParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(Codec.FLOAT.fieldOf("speed")
			.forGetter(p -> p.speed))
		.apply(i, SteamJetParticleData::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, SteamJetParticleData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, obj -> obj.speed,
		SteamJetParticleData::new
	);

	float speed;

	public SteamJetParticleData(float speed) {
		this.speed = speed;
	}

	public SteamJetParticleData() {
		this(0);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.STEAM_JET.get();
	}

	@Override
	public MapCodec<SteamJetParticleData> getCodec(ParticleType<SteamJetParticleData> type) {
		return CODEC;
	}

	@Override
	public ParticleType<SteamJetParticleData> createType() {
		SteamJetParticleData self = this;
		return new ParticleType<SteamJetParticleData>(false) {
			@Override
			public MapCodec<SteamJetParticleData> codec() {
				return self.getCodec(this);
			}
			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, SteamJetParticleData> streamCodec() {
				return self.getStreamCodec(this);
			}
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ParticleProvider<SteamJetParticleData> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public SpriteParticleRegistration<SteamJetParticleData> getMetaFactory() {
		return SteamJetParticle.Factory::new;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, SteamJetParticleData> getStreamCodec(
			ParticleType<SteamJetParticleData> type) {
		return STREAM_CODEC;
	}

}