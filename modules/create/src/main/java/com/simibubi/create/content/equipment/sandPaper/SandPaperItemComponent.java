package com.simibubi.create.content.equipment.sandPaper;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SandPaperItemComponent(ItemStack item, boolean jei) {

	public static final Codec<SandPaperItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
			ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(SandPaperItemComponent::item),
			Codec.BOOL.optionalFieldOf("jei", false).forGetter(SandPaperItemComponent::jei)
		)
		.apply(instance, SandPaperItemComponent::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SandPaperItemComponent> STREAM_CODEC =
		StreamCodec.composite(
			ItemStack.OPTIONAL_STREAM_CODEC, SandPaperItemComponent::item,
			ByteBufCodecs.BOOL, SandPaperItemComponent::jei,
			SandPaperItemComponent::new
		);

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SandPaperItemComponent other
			&& ItemStack.isSameItemSameComponents(item, other.item)
			&& jei == other.jei;
	}

	@Override
	public int hashCode() {
		return Objects.hash(item.getItem(), item.getCount(), item.getComponents(), jei);
	}
}
