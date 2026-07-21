package com.simibubi.create.foundation.mixin;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Dynamic;
import com.simibubi.create.foundation.block.DyedBlockList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix.ItemStackData;
import net.minecraft.world.item.DyeColor;;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
	
	@Inject(
		method = "fixItemStack",
		at = @At("TAIL")
	)
	private static void create$fixItemStack(ItemStackData itemStackData, Dynamic<?> tag, CallbackInfo callback) {
		itemStackData.moveTagToComponent("CollectingLight", "create:collecting_light");
		itemStackData.moveTagToComponent("InferredFromRecipe", "create:inferred_from_recipe");
		
		if(itemStackData.is("create:copper_backtank")
				|| itemStackData.is("create:copper_backtank_placeable")
				|| itemStackData.is("create:netherite_backtank") 
				|| itemStackData.is("create:netherite_backtank_placeable")) 
		{
			itemStackData.moveTagToComponent("Air", "create:air_tank");
		}
		
		if(itemStackData.is("create:belt_connector")) {
			itemStackData.moveTagToComponent("FirstPulley", "create:first_pulley");
		}
		
		if(itemStackData.is("create:display_link")) {
			itemStackData.moveTagToComponent("SelectedPos", "create:display_link_pos");
		}
		
		if(itemStackData.is("create:minecart_contraption") 
				|| itemStackData.is("create:furnace_minecart_contraption") 
				|| itemStackData.is("create:chest_minecart_contraption")) 
		{
			CompoundTag tag1 = new CompoundTag();
			itemStackData.removeTag("Contraption").result().ifPresent(arg -> tag1.put("Contraption", (Tag)arg.getValue()));
			if(!tag1.isEmpty()) itemStackData.setComponent("create:minecart_contraption", new Dynamic(NbtOps.INSTANCE, tag1));
		}
		
		//CompoundTag tag2 = new CompoundTag();
		//if(!tag2.isEmpty()) itemStackData.setComponent("create:blueprint_data", new Dynamic(NbtOps.INSTANCE, tag2));
		
		if(itemStackData.is("create:linked_controller") 
				|| itemStackData.is("create:filter") 
				|| itemStackData.is("create:attribute_filter")) 
		{
			CompoundTag tag5 = new CompoundTag();
			itemStackData.removeTag("Items").result().ifPresent(arg -> tag5.put("Items", (Tag)arg.getValue()));
			itemStackData.removeTag("RespectNBT").result().ifPresent(arg ->
				itemStackData.setComponent("create:filter_items_respect_nbt", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Blacklist").result().ifPresent(arg ->
				itemStackData.setComponent("create:filter_items_blacklist", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("WhitelistMode").result().ifPresent(arg -> tag5.put("WhitelistMode", (Tag)arg.getValue()));
			itemStackData.removeTag("MatchedAttributes").result().ifPresent(arg -> tag5.put("MatchedAttributes", (Tag)arg.getValue()));
			if(!tag5.isEmpty()) itemStackData.setComponent("create:filter_data", new Dynamic(NbtOps.INSTANCE, tag5));
		}
		
		if(itemStackData.is("create:clipboard")) {
			CompoundTag tag5 = new CompoundTag();
			itemStackData.removeTag("Readonly").result().ifPresent(arg -> tag5.put("Readonly", (Tag)arg.getValue()));
			itemStackData.removeTag("CopiedValues").result().ifPresent(arg -> tag5.put("CopiedValues", (Tag)arg.getValue()));
			itemStackData.removeTag("PreviouslyOpenedPage").result().ifPresent(arg -> tag5.put("PreviouslyOpenedPage", (Tag)arg.getValue()));
			itemStackData.removeTag("Type").result().ifPresent(arg -> tag5.put("Type", (Tag)arg.getValue()));
			itemStackData.removeTag("Pages").result().ifPresent(arg -> tag5.put("Pages", (Tag)arg.getValue()));
			if(!tag5.isEmpty()) itemStackData.setComponent("create:clipboard_editing", new Dynamic(NbtOps.INSTANCE, tag5));
		}
		
		if(itemStackData.is("create:sand_paper") || itemStackData.is("create:red_sand_paper")) {
			AtomicBoolean hasData = new AtomicBoolean(false);
			CompoundTag polishTag = new CompoundTag();
			itemStackData.removeTag("Polishing").result().ifPresent(arg -> {
				polishTag.put("item", (Tag)arg.getValue());
				hasData.set(true);
			});
			itemStackData.removeTag("JEI").result().ifPresent(arg -> {
				polishTag.putBoolean("jei", true);
				hasData.set(true);
			});
			if(hasData.get()) itemStackData.setComponent("create:polishing", new Dynamic(NbtOps.INSTANCE, polishTag));
		}
		
		if(itemStackData.is("create:wand_of_symmetry")) {
			CompoundTag symTag = new CompoundTag();
			AtomicBoolean hasSymData = new AtomicBoolean(false);
			itemStackData.removeTag("symmetry").result().ifPresent(arg -> {
				symTag.put("mirror", (Tag)arg.getValue());
				hasSymData.set(true);
			});
			itemStackData.removeTag("enable").result().ifPresent(arg -> {
				symTag.put("enable", (Tag)arg.getValue());
				hasSymData.set(true);
			});
			if(hasSymData.get()) itemStackData.setComponent("create:symmetry_wand", new Dynamic(NbtOps.INSTANCE, symTag));
		}
		
		boolean isToolbox = false;
		for (DyeColor color : DyeColor.values()) {
			if(itemStackData.is("create:" + color.getSerializedName() + "_toolbox"))
				isToolbox = true;
		}
		if(isToolbox) {
			CompoundTag tag5 = new CompoundTag();
			itemStackData.removeTag("Inventory").result().ifPresent(arg -> tag5.put("Inventory", (Tag)arg.getValue()));
			itemStackData.removeTag("UniqueId").result().ifPresent(arg -> tag5.put("UniqueId", (Tag)arg.getValue()));
			if(!tag5.isEmpty()) itemStackData.setComponent("create:toolbox", new Dynamic(NbtOps.INSTANCE, tag5));
		}
		
		if(itemStackData.is("create:handheld_worldshaper")) {
			// Enum string values: pass through directly as strings for Codec.STRING.xmap codecs
			itemStackData.removeTag("Pattern").result().ifPresent(arg ->
				itemStackData.setComponent("create:placement_pattern", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Brush").result().ifPresent(arg ->
				itemStackData.setComponent("create:shaper_brush", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Tool").result().ifPresent(arg ->
				itemStackData.setComponent("create:shaper_tool", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Placement").result().ifPresent(arg ->
				itemStackData.setComponent("create:shaper_placement_options", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			// BrushParams: stored as IntArray [x,y,z] via NbtUtils.writeBlockPos, matches BlockPos.CODEC format
			itemStackData.removeTag("BrushParams").result().ifPresent(arg ->
				itemStackData.setComponent("create:shaper_brush_params", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			// BlockUsed: CompoundTag containing serialized BlockState
			itemStackData.removeTag("BlockUsed").result().ifPresent(arg ->
				itemStackData.setComponent("create:shaper_block_used", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			// BlockData: CompoundTag containing block entity data
			itemStackData.removeTag("BlockData").result().ifPresent(arg ->
				itemStackData.setComponent("create:shaper_block_data", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			// _Swap: Boolean
			itemStackData.removeTag("_Swap").result().ifPresent(arg ->
				itemStackData.setComponent("create:shaper_swap", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
		}
		
		itemStackData.removeTag("SequencedAssembly").result().ifPresent(arg -> {
			CompoundTag oldTag = (CompoundTag) arg.getValue();
			CompoundTag newTag = new CompoundTag();
			newTag.putString("id", oldTag.getString("id"));
			newTag.putInt("step", oldTag.getInt("Step"));
			newTag.putFloat("progress", oldTag.getFloat("Progress"));
			itemStackData.setComponent("create:sequenced_assembly", new Dynamic(NbtOps.INSTANCE, newTag));
		});
		
		if(itemStackData.is("create:empty_schematic") ||
				itemStackData.is("create:schematic_and_quill") ||
				itemStackData.is("create:schematic") ||
				itemStackData.is("create:deployer"))
		{
			itemStackData.removeTag("Deployed").result().ifPresent(arg ->
				itemStackData.setComponent("create:schematic_deployed", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Owner").result().ifPresent(arg ->
				itemStackData.setComponent("create:schematic_owner", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("File").result().ifPresent(arg ->
				itemStackData.setComponent("create:schematic_file", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Anchor").result().ifPresent(arg ->
				itemStackData.setComponent("create:schematic_anchor", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Rotation").result().ifPresent(arg -> {
				String rotName = ((net.minecraft.nbt.StringTag)arg.getValue()).getAsString();
				int ordinal = net.minecraft.world.level.block.Rotation.valueOf(rotName).ordinal();
				itemStackData.setComponent("create:schematic_rotation", new Dynamic(NbtOps.INSTANCE, net.minecraft.nbt.IntTag.valueOf(ordinal)));
			});
			itemStackData.removeTag("Mirror").result().ifPresent(arg -> {
				String mirName = ((net.minecraft.nbt.StringTag)arg.getValue()).getAsString();
				int ordinal = net.minecraft.world.level.block.Mirror.valueOf(mirName).ordinal();
				itemStackData.setComponent("create:schematic_mirror", new Dynamic(NbtOps.INSTANCE, net.minecraft.nbt.IntTag.valueOf(ordinal)));
			});
			itemStackData.removeTag("Bounds").result().ifPresent(arg ->
				itemStackData.setComponent("create:schematic_bounds", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("SchematicHash").result().ifPresent(arg ->
				itemStackData.setComponent("create:schematic_hash", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
		}
		
		if(itemStackData.is("create:track") ||
				itemStackData.is("create:fake_track"))
		{
			itemStackData.removeTag("ExtendCurve").result().ifPresent(arg ->
				itemStackData.setComponent("create:track_extend_curve", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("ConnectingFrom").result().ifPresent(arg -> {
				// Old format keys: Pos, Axis, Normal, End
				// New ConnectingFrom.CODEC keys: pos, axis, normal, end (same value format)
				CompoundTag oldTag = (CompoundTag) arg.getValue();
				CompoundTag newTag = new CompoundTag();
				if(oldTag.contains("Pos")) newTag.put("pos", oldTag.get("Pos"));
				if(oldTag.contains("Axis")) newTag.put("axis", oldTag.get("Axis"));
				if(oldTag.contains("Normal")) newTag.put("normal", oldTag.get("Normal"));
				if(oldTag.contains("End")) newTag.put("end", oldTag.get("End"));
				if(!newTag.isEmpty()) itemStackData.setComponent("create:track_connecting_from", new Dynamic(NbtOps.INSTANCE, newTag));
			});
		}
		
		if(itemStackData.is("create:track_station") ||
				itemStackData.is("create:track_signal") ||
				itemStackData.is("create:track_observer"))
		{
			itemStackData.removeTag("SelectedPos").result().ifPresent(arg ->
				itemStackData.setComponent("create:track_targeting_pos", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("SelectedDirection").result().ifPresent(arg ->
				itemStackData.setComponent("create:track_targeting_direction", new Dynamic(NbtOps.INSTANCE, arg.getValue())));
			itemStackData.removeTag("Bezier").result().ifPresent(arg -> {
				// Old format: {Segment:int, Key:{X,Y,Z}, FromStack:bool}
				// New BezierTrackPointLocation.CODEC: {curveTarget:[x,y,z], segment:int}
				CompoundTag oldBezier = (CompoundTag) arg.getValue();
				CompoundTag newBezier = new CompoundTag();
				if(oldBezier.contains("Key")) newBezier.put("curveTarget", oldBezier.get("Key"));
				if(oldBezier.contains("Segment")) newBezier.putInt("segment", oldBezier.getInt("Segment"));
				itemStackData.setComponent("create:track_targeting_bezier", new Dynamic(NbtOps.INSTANCE, newBezier));
			});
		}
	}
	
}
