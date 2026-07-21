package com.simibubi.create.content.fluids.potion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import io.github.fabricators_of_create.porting_lib_ufo.fluids.FluidStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public class PotionFluid extends VirtualFluid {

	public PotionFluid(Properties properties) {
		super(properties);
	}

	public static FluidStack of(long amount, Holder<Potion> potion) {
		FluidStack fluidStack = new FluidStack(AllFluids.POTION.get()
				.getSource(), amount);
		return addPotionToFluidStack(fluidStack, potion);
	}

	public static FluidStack withEffects(long amount, Holder<Potion> potion, List<MobEffectInstance> customEffects) {
		FluidStack fluidStack = of(amount, potion);
		return appendEffects(fluidStack, customEffects);
	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, Holder<Potion> potion) {
		if (potion == null) {
			fs.remove(DataComponents.POTION_CONTENTS);
			return new FluidStack(fs.getFluid(), fs.getAmount(), (PatchedDataComponentMap)fs.getComponents());
		}
		
		ResourceLocation resourcelocation = RegisteredObjects.getKeyOrThrow(potion.value());
		fs.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return new FluidStack(fs.getFluid(), fs.getAmount(), (PatchedDataComponentMap)fs.getComponents());
	}

	public static FluidStack appendEffects(FluidStack fs, Collection<MobEffectInstance> customEffects) {
		if (customEffects.isEmpty())
			return fs;
		
		PotionContents potions = fs.getOrCreateComponent(DataComponents.POTION_CONTENTS, 
				new PotionContents(Optional.empty(), Optional.empty(), List.of()));
		for (MobEffectInstance effectinstance : customEffects)
			potions = potions.withEffectAdded(effectinstance);
		fs.set(DataComponents.POTION_CONTENTS, potions);
		return new FluidStack(fs.getFluid(), fs.getAmount(), (PatchedDataComponentMap)fs.getComponents());
	}

	public enum BottleType implements StringRepresentable {
		REGULAR, SPLASH, LINGERING;

		public static final Codec<BottleType> CODEC = StringRepresentable.fromEnum(BottleType::values);
		public static final StreamCodec<ByteBuf, BottleType> STREAM_CODEC =
				ByteBufCodecs.VAR_INT.map(BottleType::byOrdinal, BottleType::ordinal);

		public static BottleType byOrdinal(int ordinal) {
			BottleType[] values = values();
			return ordinal >= 0 && ordinal < values.length ? values[ordinal] : REGULAR;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}


}
