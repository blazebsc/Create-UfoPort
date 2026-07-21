package com.simibubi.create.foundation.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AirParticleData implements ParticleOptions, ICustomParticleDataWithSprite<AirParticleData> {

	public static final MapCodec<AirParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> 
		i.group(
			Codec.FLOAT.fieldOf("drag").forGetter(p -> p.drag),
			Codec.FLOAT.fieldOf("speed").forGetter(p -> p.speed))
		.apply(i, AirParticleData::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, AirParticleData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, obj -> obj.drag,
		ByteBufCodecs.FLOAT, obj -> obj.speed,
		AirParticleData::new
	); 
	
	float drag;
	float speed;

	public AirParticleData(float drag, float speed) {
		this.drag = drag;
		this.speed = speed;
	}

	public AirParticleData() {
		this(0, 0);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.AIR.get();
	}

	@Override
	public MapCodec<AirParticleData> getCodec(ParticleType<AirParticleData> type) {
		return CODEC;
	}

	@Override
	public ParticleType<AirParticleData> createType() {
		AirParticleData self = this;
		return new ParticleType<AirParticleData>(false) {
			@Override
			public MapCodec<AirParticleData> codec() {
				return self.getCodec(this);
			}
			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, AirParticleData> streamCodec() {
				return self.getStreamCodec(this);
			}
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ParticleProvider<AirParticleData> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public SpriteParticleRegistration<AirParticleData> getMetaFactory() {
		return AirParticle.Factory::new;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, AirParticleData> getStreamCodec(ParticleType<AirParticleData> type) {
		return STREAM_CODEC;
	}

}