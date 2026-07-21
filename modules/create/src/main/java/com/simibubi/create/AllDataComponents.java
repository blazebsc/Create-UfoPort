package com.simibubi.create;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.equipment.clipboard.ClipboardData;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe.SequencedAssembly;
import com.simibubi.create.content.equipment.zapper.PlacementPatterns;
import com.simibubi.create.content.equipment.zapper.terrainzapper.PlacementOptions;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainBrushes;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainTools;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu.WhitelistMode;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.trains.track.TrackPlacement.ConnectingFrom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.ItemContainerContents;

public class AllDataComponents {

	private static final Codec<SymmetryMirror> LEGACY_SYM_WAND_CODEC =
		RecordCodecBuilder.create(i -> i.group(
			CompoundTag.CODEC.fieldOf("mirror").forGetter(SymmetryMirror::writeToNbt)
		).apply(i, SymmetryMirror::fromNBT));

	// TODO: Forge uses CompoundTag here too. Contraption data is inherently dynamic/complex.
	public static DataComponentType<CompoundTag> MINECART_CONTRAPTION = null;

	// Typed wrapper for clipboard content data (pages, type, readonly, copiedValues, previouslyOpenedPage).
	// Forge uses a fully decomposed ClipboardContent record; UfoPort wraps CompoundTag for pragmatic compat.
	public static DataComponentType<ClipboardData> CLIPBOARD_EDITING = null;

	// Stores filter items (Items CompoundTag), attribute filter WhitelistMode/MatchedAttributes.
	// Forge splits further into FILTER_ITEMS (ItemContainerContents) and attribute components,
	// but UfoPort lacks ItemContainerContents helpers and typed attribute filter types.
	public static DataComponentType<CompoundTag> FILTER_DATA = null;
	public static DataComponentType<ItemContainerContents> FILTER_ITEMS = null;
	public static DataComponentType<WhitelistMode> ATTRIBUTE_FILTER_WHITELIST_MODE = null;
	public static DataComponentType<List<CompoundTag>> ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES = null;

	// Whether a regular filter should match NBT data
	public static DataComponentType<Boolean> FILTER_ITEMS_RESPECT_NBT = null;

	// Whether a regular filter is a deny list (blacklist)
	public static DataComponentType<Boolean> FILTER_ITEMS_BLACKLIST = null;

	// Stores entity customization data for blueprint hanging entities
	public static DataComponentType<CompoundTag> BLUEPRINT_DATA = null;

	// Strongly typed: stores the item being polished and JEI display flag
	public static DataComponentType<SandPaperItemComponent> POLISHING = null;

	// Strongly typed: stores symmetry mirror itself
	public static DataComponentType<SymmetryMirror> SYM_WAND = null;
	public static DataComponentType<Boolean> SYM_WAND_ENABLE = null;
	public static DataComponentType<Boolean> SYM_WAND_SIMULATE = null;

	// Split toolbox data: inventory tag + stable UUID
	public static DataComponentType<CompoundTag> TOOLBOX_INVENTORY = null;
	public static DataComponentType<UUID> TOOLBOX_UUID = null;
	// Deprecated legacy combined toolbox component kept for migration of existing item data.
	public static DataComponentType<CompoundTag> TOOLBOX_LEGACY = null;

	public static DataComponentType<PlacementPatterns> PLACEMENT_PATTERN = null;
	public static DataComponentType<TerrainBrushes> SHAPER_BRUSH = null;
	public static DataComponentType<BlockPos> SHAPER_BRUSH_PARAMS = null;
	public static DataComponentType<PlacementOptions> SHAPER_PLACEMENT_OPTIONS = null;
	public static DataComponentType<TerrainTools> SHAPER_TOOL = null;
	public static DataComponentType<BlockState> SHAPER_BLOCK_USED = null;
	public static DataComponentType<Boolean> SHAPER_SWAP = null;
	public static DataComponentType<CompoundTag> SHAPER_BLOCK_DATA = null;

	// Strongly typed: stores BottleType enum directly (REGULAR, SPLASH, LINGERING)
	public static DataComponentType<BottleType> BOTTLE_TYPE = null;

	// Strongly typed: stores recipe ID, step, and progress
	public static DataComponentType<SequencedAssembly> SEQUENCED_ASSEMBLY = null;

	public static DataComponentType<Boolean> SCHEMATIC_DEPLOYED = null;
	public static DataComponentType<String> SCHEMATIC_OWNER = null;
	public static DataComponentType<String> SCHEMATIC_FILE = null;
	public static DataComponentType<BlockPos> SCHEMATIC_ANCHOR = null;
	public static DataComponentType<Rotation> SCHEMATIC_ROTATION = null;
	public static DataComponentType<Mirror> SCHEMATIC_MIRROR = null;
	public static DataComponentType<Vec3i> SCHEMATIC_BOUNDS = null;
	public static DataComponentType<Integer> SCHEMATIC_HASH = null;

	// TODO: Forge also uses CompoundTag (TRAIN_SCHEDULE). Schedule data is complex and dynamic.
	public static DataComponentType<CompoundTag> SCHEDULE_DATA = null;

	// Strongly typed: stores track connection origin data (pos, axis, normal, end)
	public static DataComponentType<ConnectingFrom> TRACK_CONNECTING_FROM = null;

	// Strongly typed: whether to extend curve when placing
	public static DataComponentType<Boolean> TRACK_EXTENDED_CURVE = null;

	// Strongly typed: track targeting selected position
	public static DataComponentType<BlockPos> TRACK_TARGETING_POS = null;

	// Strongly typed: track targeting selected direction (true = positive)
	public static DataComponentType<Boolean> TRACK_TARGETING_DIRECTION = null;

	// Strongly typed: track targeting bezier curve location
	public static DataComponentType<BezierTrackPointLocation> TRACK_TARGETING_BEZIER = null;
	public static DataComponentType<ItemContainerContents> LINKED_CONTROLLER_ITEMS = null;

	public static DataComponentType<BlockPos> DISPLAY_LINK_POS = null;
	public static DataComponentType<BlockPos> FIRST_PULLEY = null;
	public static DataComponentType<Integer> AIR_TANK = null;
	public static DataComponentType<Boolean> INFERRED_FROM_RECIPE = null;
	public static DataComponentType<Integer> COLLECTING_LIGHT = null;

	@SuppressWarnings("unchecked")
	private static <T> DataComponentType<T> register(String name, UnaryOperator<Builder<T>> builderOperator) {
		DataComponentType<T> type = builderOperator.apply(DataComponentType.builder()).build();
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "create:" + name, type);
		return type;
	}

	public static void register() {
		MINECART_CONTRAPTION = register("minecart_contraption",
				(UnaryOperator<Builder<CompoundTag>>) builder -> builder
						.persistent(CompoundTag.CODEC)
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
						.cacheEncoding());

		CLIPBOARD_EDITING = register("clipboard_editing",
				(UnaryOperator<Builder<ClipboardData>>) builder -> builder
						.persistent(ClipboardData.CODEC)
						.networkSynchronized(ClipboardData.STREAM_CODEC)
						.cacheEncoding());

		FILTER_DATA = register("filter_data",
				(UnaryOperator<Builder<CompoundTag>>) builder -> builder
						.persistent(CompoundTag.CODEC)
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
						.cacheEncoding());

		FILTER_ITEMS = register("filter_items",
				(UnaryOperator<Builder<ItemContainerContents>>) builder -> builder
						.persistent(ItemContainerContents.CODEC)
						.networkSynchronized(ItemContainerContents.STREAM_CODEC)
						.cacheEncoding());

		ATTRIBUTE_FILTER_WHITELIST_MODE = register("attribute_filter_whitelist_mode",
				(UnaryOperator<Builder<WhitelistMode>>) builder -> builder
						.persistent(WhitelistMode.CODEC)
						.networkSynchronized(WhitelistMode.STREAM_CODEC)
						.cacheEncoding());

		ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES = register("attribute_filter_matched_attributes",
				(UnaryOperator<Builder<List<CompoundTag>>>) builder -> builder
						.persistent(CompoundTag.CODEC.listOf())
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()))
						.cacheEncoding());

		FILTER_ITEMS_RESPECT_NBT = register("filter_items_respect_nbt",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		FILTER_ITEMS_BLACKLIST = register("filter_items_blacklist",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		BLUEPRINT_DATA = register("blueprint_data",
				(UnaryOperator<Builder<CompoundTag>>) builder -> builder
						.persistent(CompoundTag.CODEC)
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
						.cacheEncoding());

		POLISHING = register("polishing",
				(UnaryOperator<Builder<SandPaperItemComponent>>) builder -> builder
						.persistent(SandPaperItemComponent.CODEC)
						.networkSynchronized(SandPaperItemComponent.STREAM_CODEC)
						.cacheEncoding());

		SYM_WAND = register("symmetry_wand",
				(UnaryOperator<Builder<SymmetryMirror>>) builder -> builder
						.persistent(Codec.withAlternative(SymmetryMirror.CODEC, LEGACY_SYM_WAND_CODEC))
						.networkSynchronized(SymmetryMirror.STREAM_CODEC)
						.cacheEncoding());

		SYM_WAND_ENABLE = register("symmetry_wand_enable",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		SYM_WAND_SIMULATE = register("symmetry_wand_simulate",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		TOOLBOX_INVENTORY = register("toolbox_inventory",
				(UnaryOperator<Builder<CompoundTag>>) builder -> builder
						.persistent(CompoundTag.CODEC)
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
						.cacheEncoding());

		TOOLBOX_UUID = register("toolbox_uuid",
				(UnaryOperator<Builder<UUID>>) builder -> builder
						.persistent(UUIDUtil.CODEC)
						.networkSynchronized(UUIDUtil.STREAM_CODEC)
						.cacheEncoding());

		TOOLBOX_LEGACY = register("toolbox",
				(UnaryOperator<Builder<CompoundTag>>) builder -> builder
						.persistent(CompoundTag.CODEC)
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
						.cacheEncoding());

		PLACEMENT_PATTERN = register("placement_pattern",
				(UnaryOperator<Builder<PlacementPatterns>>) builder -> builder
						.persistent(PlacementPatterns.CODEC)
						.networkSynchronized(PlacementPatterns.STREAM_CODEC)
						.cacheEncoding());

		SHAPER_BRUSH = register("shaper_brush",
				(UnaryOperator<Builder<TerrainBrushes>>) builder -> builder
						.persistent(TerrainBrushes.CODEC)
						.networkSynchronized(TerrainBrushes.STREAM_CODEC)
						.cacheEncoding());

		SHAPER_BRUSH_PARAMS = register("shaper_brush_params",
				(UnaryOperator<Builder<BlockPos>>) builder -> builder
						.persistent(BlockPos.CODEC)
						.networkSynchronized(BlockPos.STREAM_CODEC)
						.cacheEncoding());

		SHAPER_PLACEMENT_OPTIONS = register("shaper_placement_options",
				(UnaryOperator<Builder<PlacementOptions>>) builder -> builder
						.persistent(PlacementOptions.CODEC)
						.networkSynchronized(PlacementOptions.STREAM_CODEC)
						.cacheEncoding());

		SHAPER_TOOL = register("shaper_tool",
				(UnaryOperator<Builder<TerrainTools>>) builder -> builder
						.persistent(TerrainTools.CODEC)
						.networkSynchronized(TerrainTools.STREAM_CODEC)
						.cacheEncoding());

		SHAPER_BLOCK_USED = register("shaper_block_used",
				(UnaryOperator<Builder<BlockState>>) builder -> builder
						.persistent(BlockState.CODEC)
						.networkSynchronized(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY))
						.cacheEncoding());

		SHAPER_SWAP = register("shaper_swap",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		SHAPER_BLOCK_DATA = register("shaper_block_data",
				(UnaryOperator<Builder<CompoundTag>>) builder -> builder
						.persistent(CompoundTag.CODEC)
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
						.cacheEncoding());

		BOTTLE_TYPE = register("bottle_type",
				(UnaryOperator<Builder<BottleType>>) builder -> builder
						.persistent(BottleType.CODEC)
						.networkSynchronized(BottleType.STREAM_CODEC)
						.cacheEncoding());

		SEQUENCED_ASSEMBLY = register("sequenced_assembly",
				(UnaryOperator<Builder<SequencedAssembly>>) builder -> builder
						.persistent(SequencedAssembly.CODEC)
						.networkSynchronized(SequencedAssembly.STREAM_CODEC)
						.cacheEncoding());

		SCHEMATIC_DEPLOYED = register("schematic_deployed",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		SCHEMATIC_OWNER = register("schematic_owner",
				(UnaryOperator<Builder<String>>) builder -> builder
						.persistent(Codec.STRING)
						.networkSynchronized(ByteBufCodecs.STRING_UTF8)
						.cacheEncoding());

		SCHEMATIC_FILE = register("schematic_file",
				(UnaryOperator<Builder<String>>) builder -> builder
						.persistent(Codec.STRING)
						.networkSynchronized(ByteBufCodecs.STRING_UTF8)
						.cacheEncoding());

		SCHEMATIC_ANCHOR = register("schematic_anchor",
				(UnaryOperator<Builder<BlockPos>>) builder -> builder
						.persistent(BlockPos.CODEC)
						.networkSynchronized(BlockPos.STREAM_CODEC)
						.cacheEncoding());

		SCHEMATIC_ROTATION = register("schematic_rotation",
				(UnaryOperator<Builder<Rotation>>) builder -> builder
						.persistent(Rotation.CODEC)
						.networkSynchronized(ByteBufCodecs.VAR_INT.map(i -> Rotation.values()[i], Rotation::ordinal))
						.cacheEncoding());

		SCHEMATIC_MIRROR = register("schematic_mirror",
				(UnaryOperator<Builder<Mirror>>) builder -> builder
						.persistent(Mirror.CODEC)
						.networkSynchronized(ByteBufCodecs.VAR_INT.map(i -> Mirror.values()[i], Mirror::ordinal))
						.cacheEncoding());

		SCHEMATIC_BOUNDS = register("schematic_bounds",
				(UnaryOperator<Builder<Vec3i>>) builder -> builder
						.persistent(Vec3i.CODEC)
						.networkSynchronized(StreamCodec.composite(
								ByteBufCodecs.INT, Vec3i::getX,
								ByteBufCodecs.INT, Vec3i::getY,
								ByteBufCodecs.INT, Vec3i::getZ,
								Vec3i::new))
						.cacheEncoding());

		SCHEMATIC_HASH = register("schematic_hash",
				(UnaryOperator<Builder<Integer>>) builder -> builder
						.persistent(Codec.INT)
						.networkSynchronized(ByteBufCodecs.INT)
						.cacheEncoding());

		SCHEDULE_DATA = register("schedule_data",
				(UnaryOperator<Builder<CompoundTag>>) builder -> builder
						.persistent(CompoundTag.CODEC)
						.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
						.cacheEncoding());

		TRACK_CONNECTING_FROM = register("track_connecting_from",
				(UnaryOperator<Builder<ConnectingFrom>>) builder -> builder
						.persistent(ConnectingFrom.CODEC)
						.networkSynchronized(ConnectingFrom.STREAM_CODEC)
						.cacheEncoding());

		TRACK_EXTENDED_CURVE = register("track_extend_curve",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		TRACK_TARGETING_POS = register("track_targeting_pos",
				(UnaryOperator<Builder<BlockPos>>) builder -> builder
						.persistent(BlockPos.CODEC)
						.networkSynchronized(BlockPos.STREAM_CODEC)
						.cacheEncoding());

		TRACK_TARGETING_DIRECTION = register("track_targeting_direction",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		TRACK_TARGETING_BEZIER = register("track_targeting_bezier",
				(UnaryOperator<Builder<BezierTrackPointLocation>>) builder -> builder
						.persistent(BezierTrackPointLocation.CODEC)
						.networkSynchronized(BezierTrackPointLocation.STREAM_CODEC)
						.cacheEncoding());

		LINKED_CONTROLLER_ITEMS = register("linked_controller_items",
				(UnaryOperator<Builder<ItemContainerContents>>) builder -> builder
						.persistent(ItemContainerContents.CODEC)
						.networkSynchronized(ItemContainerContents.STREAM_CODEC)
						.cacheEncoding());

		FIRST_PULLEY = register("first_pulley",
				(UnaryOperator<Builder<BlockPos>>) builder -> builder
						.persistent(BlockPos.CODEC)
						.networkSynchronized(BlockPos.STREAM_CODEC)
						.cacheEncoding());

		DISPLAY_LINK_POS = register("display_link_pos",
				(UnaryOperator<Builder<BlockPos>>) builder -> builder
						.persistent(BlockPos.CODEC)
						.networkSynchronized(BlockPos.STREAM_CODEC)
						.cacheEncoding());

		AIR_TANK = register("air_tank",
				(UnaryOperator<Builder<Integer>>) builder -> builder
						.persistent(Codec.INT)
						.networkSynchronized(ByteBufCodecs.INT)
						.cacheEncoding());

		INFERRED_FROM_RECIPE = register("inferred_from_recipe",
				(UnaryOperator<Builder<Boolean>>) builder -> builder
						.persistent(Codec.BOOL)
						.networkSynchronized(ByteBufCodecs.BOOL)
						.cacheEncoding());

		COLLECTING_LIGHT = register("collecting_light",
				(UnaryOperator<Builder<Integer>>) builder -> builder
						.persistent(Codec.INT)
						.networkSynchronized(ByteBufCodecs.INT)
						.cacheEncoding());
	}

}
