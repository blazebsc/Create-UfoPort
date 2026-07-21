package com.simibubi.create.content.equipment.zapper.terrainzapper;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum TerrainBrushes {

	Cuboid(new CuboidBrush()),
	Sphere(new SphereBrush()),
	Cylinder(new CylinderBrush()),
	Surface(new DynamicBrush(true)),
	Cluster(new DynamicBrush(false)),

	;

	public static final Codec<TerrainBrushes> CODEC = Codec.STRING.xmap(TerrainBrushes::valueOf, TerrainBrushes::name);
	public static final StreamCodec<ByteBuf, TerrainBrushes> STREAM_CODEC =
		ByteBufCodecs.VAR_INT.map(i -> TerrainBrushes.values()[i], Enum::ordinal);

	private Brush brush;

	private TerrainBrushes(Brush brush) {
		this.brush = brush;
	}

	public Brush get() {
		return brush;
	}

}
