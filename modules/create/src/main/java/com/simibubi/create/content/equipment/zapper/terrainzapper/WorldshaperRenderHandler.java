package com.simibubi.create.content.equipment.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class WorldshaperRenderHandler {

	private static Supplier<Collection<BlockPos>> renderedPositions;

	public static void tick() {
		gatherSelectedBlocks();
		if (renderedPositions == null)
			return;

		CreateClient.OUTLINER.showCluster("terrainZapper", renderedPositions.get())
				.colored(0xbfbfbf)
				.disableLineNormals()
				.lineWidth(1 / 32f)
				.withFaceTexture(AllSpecialTextures.CHECKERED);
	}

	protected static void gatherSelectedBlocks() {
		LocalPlayer player = Minecraft.getInstance().player;
		ItemStack heldMain = player.getMainHandItem();
		ItemStack heldOff = player.getOffhandItem();
		boolean zapperInMain = AllItems.WORLDSHAPER.isIn(heldMain);
		boolean zapperInOff = AllItems.WORLDSHAPER.isIn(heldOff);

		if (zapperInMain) {
			boolean isSwap = heldMain.getOrDefault(AllDataComponents.SHAPER_SWAP, false);
			if (!isSwap || !zapperInOff) {
				createBrushOutline(heldMain, player);
				return;
			}
		}

		if (zapperInOff) {
			createBrushOutline(heldOff, player);
			return;
		}

		renderedPositions = null;
	}

	public static void createBrushOutline(ItemStack zapper, LocalPlayer player) {
		if (!zapper.has(AllDataComponents.SHAPER_BRUSH_PARAMS)) {
			renderedPositions = null;
			return;
		}

		Brush brush = zapper.getOrDefault(AllDataComponents.SHAPER_BRUSH, TerrainBrushes.Cuboid).get();
		PlacementOptions placement = zapper.getOrDefault(AllDataComponents.SHAPER_PLACEMENT_OPTIONS, PlacementOptions.Merged);
		TerrainTools tool = zapper.getOrDefault(AllDataComponents.SHAPER_TOOL, TerrainTools.Fill);
		BlockPos params = zapper.get(AllDataComponents.SHAPER_BRUSH_PARAMS);
		brush.set(params.getX(), params.getY(), params.getZ());

		Vec3 start = player.position()
			.add(0, player.getEyeHeight(), 0);
		Vec3 range = player.getLookAngle()
			.scale(128);
		BlockHitResult raytrace = player.level()
			.clip(new ClipContext(start, start.add(range), Block.OUTLINE, Fluid.NONE, player));
		if (raytrace == null || raytrace.getType() == Type.MISS) {
			renderedPositions = null;
			return;
		}

		BlockPos pos = raytrace.getBlockPos()
			.offset(brush.getOffset(player.getLookAngle(), raytrace.getDirection(), placement));
		renderedPositions =
			() -> brush.addToGlobalPositions(player.level(), pos, raytrace.getDirection(), new ArrayList<>(), tool);
	}

}
