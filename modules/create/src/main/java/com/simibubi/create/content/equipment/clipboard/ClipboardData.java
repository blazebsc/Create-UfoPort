package com.simibubi.create.content.equipment.clipboard;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllDataComponents;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ClipboardData(CompoundTag tag) {

	public static final ClipboardData EMPTY = new ClipboardData(new CompoundTag());

	public static final Codec<ClipboardData> CODEC =
		CompoundTag.CODEC.xmap(ClipboardData::new, ClipboardData::tag);

	public static final StreamCodec<ByteBuf, ClipboardData> STREAM_CODEC =
		ByteBufCodecs.COMPOUND_TAG.map(ClipboardData::new, ClipboardData::tag);

	public boolean isEmpty() {
		return tag.isEmpty();
	}

	public static CompoundTag getOrCreate(ItemStack stack) {
		ClipboardData data = stack.get(AllDataComponents.CLIPBOARD_EDITING);
		if (data == null) {
			data = new ClipboardData(new CompoundTag());
			stack.set(AllDataComponents.CLIPBOARD_EDITING, data);
		}
		return data.tag();
	}
}
