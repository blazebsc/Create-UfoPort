package com.simibubi.create.content.trains.track;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraphHelper;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackTargetingBlockItem extends BlockItem {

	private EdgePointType<?> type;

	public static <T extends Block> NonNullBiFunction<? super T, Item.Properties, TrackTargetingBlockItem> ofType(
		EdgePointType<?> type) {
		return (b, p) -> new TrackTargetingBlockItem(b, p, type);
	}

	public TrackTargetingBlockItem(Block pBlock, Properties pProperties, EdgePointType<?> type) {
		super(pBlock, pProperties);
		this.type = type;
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		ItemStack stack = pContext.getItemInHand();
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		BlockState state = level.getBlockState(pos);
		Player player = pContext.getPlayer();

		if (player == null)
			return InteractionResult.FAIL;

		if (player.isShiftKeyDown() && stack.has(AllDataComponents.TRACK_TARGETING_POS)) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			player.displayClientMessage(Lang.translateDirect("track_target.clear"), true);
			stack.remove(AllDataComponents.TRACK_TARGETING_POS);
			stack.remove(AllDataComponents.TRACK_TARGETING_DIRECTION);
			stack.remove(AllDataComponents.TRACK_TARGETING_BEZIER);
			stack.remove(DataComponents.BLOCK_ENTITY_DATA);
			AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, .5f);
			return InteractionResult.SUCCESS;
		}

		if (state.getBlock() instanceof ITrackBlock track) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;

			Vec3 lookAngle = player.getLookAngle();
			boolean front = track.getNearestTrackAxis(level, pos, state, lookAngle)
				.getSecond() == AxisDirection.POSITIVE;
			EdgePointType<?> type = getType(stack);

			MutableObject<OverlapResult> result = new MutableObject<>(null);
			withGraphLocation(level, pos, front, null, type, (overlap, location) -> result.setValue(overlap));

			if (result.getValue().feedback != null) {
				player.displayClientMessage(Lang.translateDirect(result.getValue().feedback)
					.withStyle(ChatFormatting.RED), true);
				AllSoundEvents.DENY.play(level, null, pos, .5f, 1);
				return InteractionResult.FAIL;
			}

			stack.set(AllDataComponents.TRACK_TARGETING_POS, pos);
			stack.set(AllDataComponents.TRACK_TARGETING_DIRECTION, front);
			stack.remove(AllDataComponents.TRACK_TARGETING_BEZIER);
			player.displayClientMessage(Lang.translateDirect("track_target.set"), true);
			AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1);
			return InteractionResult.SUCCESS;
		}

		if (!stack.has(AllDataComponents.TRACK_TARGETING_POS)) {
			player.displayClientMessage(Lang.translateDirect("track_target.missing")
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		CompoundTag teTag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(new CompoundTag())).copyTag();
		boolean direction = stack.getOrDefault(AllDataComponents.TRACK_TARGETING_DIRECTION, false);
		teTag.putBoolean("TargetDirection", direction);

		BlockPos selectedPos = stack.getOrDefault(AllDataComponents.TRACK_TARGETING_POS, BlockPos.ZERO);
		BlockPos placedPos = pos.relative(pContext.getClickedFace(), state.canBeReplaced() ? 0 : 1);

		boolean bezier = stack.has(AllDataComponents.TRACK_TARGETING_BEZIER);

		if (!selectedPos.closerThan(placedPos, bezier ? 64 + 16 : 16)) {
			player.displayClientMessage(Lang.translateDirect("track_target.too_far")
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		if (bezier) {
			BezierTrackPointLocation bezierLoc = stack.get(AllDataComponents.TRACK_TARGETING_BEZIER);
			CompoundTag bezierNbt = new CompoundTag();
			bezierNbt.putInt("Segment", bezierLoc.segment());
			bezierNbt.put("Key", NbtUtils.writeBlockPos(bezierLoc.curveTarget()));
			bezierNbt.putBoolean("FromStack", true);
			teTag.put("Bezier", bezierNbt);
		}

		teTag.put("TargetTrack", NbtUtils.writeBlockPos(selectedPos.subtract(placedPos)));
		stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(teTag));

		InteractionResult useOn = super.useOn(pContext);
		if (level.isClientSide || useOn == InteractionResult.FAIL)
			return useOn;

		ItemStack itemInHand = player.getItemInHand(pContext.getHand());
		if (!itemInHand.isEmpty()) {
			itemInHand.remove(AllDataComponents.TRACK_TARGETING_POS);
			itemInHand.remove(AllDataComponents.TRACK_TARGETING_DIRECTION);
			itemInHand.remove(AllDataComponents.TRACK_TARGETING_BEZIER);
			itemInHand.remove(DataComponents.BLOCK_ENTITY_DATA);
		}
		player.displayClientMessage(Lang.translateDirect("track_target.success")
			.withStyle(ChatFormatting.GREEN), true);

		if (type == EdgePointType.SIGNAL)
			AllAdvancements.SIGNAL.awardTo(player);

		return useOn;
	}

	public EdgePointType<?> getType(ItemStack stack) {
		return type;
	}

	@Environment(EnvType.CLIENT)
	public boolean useOnCurve(BezierPointSelection selection, ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		TrackBlockEntity be = selection.blockEntity();
		BezierTrackPointLocation loc = selection.loc();
		boolean front = player.getLookAngle()
			.dot(selection.direction()) < 0;

		AllPackets.getChannel().sendToServer(new CurvedTrackSelectionPacket(be.getBlockPos(), loc.curveTarget(),
			loc.segment(), front, player.getInventory().selected));
		return true;
	}

	public static enum OverlapResult {

		VALID,
		OCCUPIED("track_target.occupied"),
		JUNCTION("track_target.no_junctions"),
		NO_TRACK("track_target.invalid");

		public String feedback;

		private OverlapResult() {}

		private OverlapResult(String feedback) {
			this.feedback = feedback;
		}

	}

	public static void withGraphLocation(Level level, BlockPos pos, boolean front,
		BezierTrackPointLocation targetBezier, EdgePointType<?> type,
		BiConsumer<OverlapResult, TrackGraphLocation> callback) {

		BlockState state = level.getBlockState(pos);

		if (!(state.getBlock() instanceof ITrackBlock track)) {
			callback.accept(OverlapResult.NO_TRACK, null);
			return;
		}

		List<Vec3> trackAxes = track.getTrackAxes(level, pos, state);
		if (targetBezier == null && trackAxes.size() > 1) {
			callback.accept(OverlapResult.JUNCTION, null);
			return;
		}

		AxisDirection targetDirection = front ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		TrackGraphLocation location =
			targetBezier != null ? TrackGraphHelper.getBezierGraphLocationAt(level, pos, targetDirection, targetBezier)
				: TrackGraphHelper.getGraphLocationAt(level, pos, targetDirection, trackAxes.get(0));

		if (location == null) {
			callback.accept(OverlapResult.NO_TRACK, null);
			return;
		}

		Couple<TrackNode> nodes = location.edge.map(location.graph::locateNode);
		TrackEdge edge = location.graph.getConnection(nodes);
		if (edge == null)
			return;

		EdgeData edgeData = edge.getEdgeData();
		double edgePosition = location.position;

		for (TrackEdgePoint edgePoint : edgeData.getPoints()) {
			double otherEdgePosition = edgePoint.getLocationOn(edge);
			double distance = Math.abs(edgePosition - otherEdgePosition);
			if (distance > .75)
				continue;
			if (edgePoint.canCoexistWith(type, front) && distance < .25)
				continue;

			callback.accept(OverlapResult.OCCUPIED, location);
			return;
		}

		callback.accept(OverlapResult.VALID, location);
	}

}
