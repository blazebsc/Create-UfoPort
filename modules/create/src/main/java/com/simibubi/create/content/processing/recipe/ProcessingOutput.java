package com.simibubi.create.content.processing.recipe;

import java.util.Optional;
import java.util.Random;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ProcessingOutput {

	public static final ProcessingOutput EMPTY = new ProcessingOutput(ItemStack.EMPTY, 1);

	private static final Random r = new Random();
	private final ItemStack stack;
	private final float chance;
	
	public static final Codec<ProcessingOutput> CODEC_LEFT = RecordCodecBuilder.create(
			instance -> instance.group(
					ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(po -> po.stack.getItemHolder()),
					Codec.INT.optionalFieldOf("count").forGetter(po -> po.stack.getCount() == 1 ? 
							Optional.empty() : Optional.ofNullable(po.stack.getCount())),
					DataComponentPatch.CODEC.optionalFieldOf("components").forGetter(obj -> 
							obj.stack.getComponents().isEmpty() || ((PatchedDataComponentMap)obj.stack.getComponents()).asPatch().isEmpty() ? 
									Optional.empty() : Optional.of(((PatchedDataComponentMap)obj.stack.getComponents()).asPatch())),
					Codec.FLOAT.optionalFieldOf("chance").forGetter(po -> po.chance == 1 ? 
							Optional.empty() : Optional.ofNullable(po.chance))
			).apply(instance, ProcessingOutput::new)
	);
	
	public static final Codec<ProcessingOutput> CODEC_RIGHT = RecordCodecBuilder.create(
			instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("id").forGetter(po -> po.compatDatagenOutput.getFirst()),
					Codec.INT.optionalFieldOf("count").forGetter(po -> po.compatDatagenOutput.getSecond() == 1 ? 
							Optional.empty() : Optional.ofNullable(po.compatDatagenOutput.getSecond())),
					Codec.FLOAT.optionalFieldOf("chance").forGetter(po -> po.chance == 1 ? 
							Optional.empty() : Optional.ofNullable(po.chance))
			).apply(instance, (id, co, ch) -> new ProcessingOutput(Pair.of(id, co.isEmpty() ? 1 : co.get()), ch.isEmpty() ? 1 : ch.get()))
	);
	
	public static final Codec<ProcessingOutput> CODEC = Codec.either(CODEC_LEFT, CODEC_RIGHT)
			.xmap(either -> either.map(a -> a, a -> a), procout -> {
				if(!procout.stack.isEmpty())
					return Either.left(procout);
				if(procout.compatDatagenOutput != null)
					return Either.right(procout);
				throw new IllegalArgumentException("ProcessingOutput: stack is empty && compatDatagen == null!");
			});
	
	public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingOutput> STREAM_CODEC = StreamCodec.composite(
		ItemStack.STREAM_CODEC, ProcessingOutput::getStack,
		ByteBufCodecs.FLOAT, ProcessingOutput::getChance,
		ProcessingOutput::new
	);

	private Pair<ResourceLocation, Integer> compatDatagenOutput;
	
	public ProcessingOutput(Holder<Item> stack, Optional<Integer> count, Optional<DataComponentPatch> tag, Optional<Float> chance) {
		this.stack = new ItemStack(stack.value(), 1);
		this.stack.setCount(count.isEmpty() ? 1 : count.get());
		if(tag.isPresent())
			this.stack.applyComponents(tag.get());
		this.chance = chance.isEmpty() ? 1 : chance.get();
	}

	public ProcessingOutput(ItemStack stack, float chance) {
		this.stack = stack;
		this.chance = chance;
	}

	public ProcessingOutput(Pair<ResourceLocation, Integer> item, float chance) {
		this.stack = ItemStack.EMPTY;
		this.compatDatagenOutput = item;
		this.chance = chance;
	}

	public ItemStack getStack() {
		return stack;
	}

	public float getChance() {
		return chance;
	}

	public ItemStack rollOutput() {
		int outputAmount = stack.getCount();
		for (int roll = 0; roll < stack.getCount(); roll++)
			if (r.nextFloat() > chance)
				outputAmount--;
		if (outputAmount == 0)
			return ItemStack.EMPTY;
		ItemStack out = stack.copy();
		out.setCount(outputAmount);
		return out;
	}

	// Serialization is handled by CODEC and STREAM_CODEC defined above.

}
