package com.simibubi.create.content.kinetics.fan;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AirFlowParticleData implements ParticleOptions, ICustomParticleDataWithSprite<AirFlowParticleData> {

	public static final MapCodec<AirFlowParticleData> CODEC = RecordCodecBuilder.mapCodec(
			i -> i.group(Codec.INT.fieldOf("x").forGetter(p -> p.posX), Codec.INT.fieldOf("y").forGetter(p -> p.posY),
					Codec.INT.fieldOf("z").forGetter(p -> p.posZ)).apply(i, AirFlowParticleData::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, AirFlowParticleData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, obj -> obj.posX,
		ByteBufCodecs.INT, obj -> obj.posY,
		ByteBufCodecs.INT, obj -> obj.posZ,
		AirFlowParticleData::new
	);

	final int posX;
	final int posY;
	final int posZ;

	public AirFlowParticleData(Vec3i pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}

	public AirFlowParticleData(int posX, int posY, int posZ) {
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}

	public AirFlowParticleData() {
		this(0, 0, 0);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.AIR_FLOW.get();
	}

	@Override
	public MapCodec<AirFlowParticleData> getCodec(ParticleType<AirFlowParticleData> type) {
		return CODEC;
	}

	@Override
	public ParticleType<AirFlowParticleData> createType() {
		AirFlowParticleData self = this;
		return new ParticleType<AirFlowParticleData>(false) {
			@Override
			public MapCodec<AirFlowParticleData> codec() {
				return self.getCodec(this);
			}
			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, AirFlowParticleData> streamCodec() {
				return self.getStreamCodec(this);
			}
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ParticleProvider<AirFlowParticleData> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public SpriteParticleRegistration<AirFlowParticleData> getMetaFactory() {
		return AirFlowParticle.Factory::new;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, AirFlowParticleData> getStreamCodec(
			ParticleType<AirFlowParticleData> type) {
		return STREAM_CODEC;
	}

}