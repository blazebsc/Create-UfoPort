package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.foundation.utility.Pair;

import io.github.fabricators_of_create.porting_lib_ufo.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib_ufo.transfer.item.ItemStackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FilterItemStack {

	private ItemStack filterItemStack;
	private boolean fluidExtracted;
	private FluidStack filterFluidStack;

	public static FilterItemStack of(ItemStack filter) {
		if (filter.has(AllDataComponents.FILTER_ITEMS) || filter.has(AllDataComponents.FILTER_DATA)
			|| filter.has(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES)) {
			if (AllItems.FILTER.isIn(filter))
				return new ListFilterItemStack(filter);
			if (AllItems.ATTRIBUTE_FILTER.isIn(filter))
				return new AttributeFilterItemStack(filter);
		}

		return new FilterItemStack(filter);
	}

	public static FilterItemStack of(CompoundTag tag) {
		return of(ItemStack.parseOptional(Create.getRegistryAccess(), tag));
	}

	public static FilterItemStack empty() {
		return of(ItemStack.EMPTY);
	}

	public boolean isEmpty() {
		return filterItemStack.isEmpty();
	}

	public CompoundTag serializeNBT() {
		CompoundTag ret = (CompoundTag)filterItemStack.saveOptional(Create.getRegistryAccess());
		return ret;
	}

	public ItemStack item() {
		return filterItemStack;
	}

	public FluidStack fluid(Level level) {
		resolveFluid(level);
		return filterFluidStack;
	}

	public boolean isFilterItem() {
		return filterItemStack.getItem() instanceof FilterItem;
	}

	//

	public boolean test(Level world, ItemStack stack) {
		return test(world, stack, false);
	}

	public boolean test(Level world, FluidStack stack) {
		return test(world, stack, true);
	}

	public boolean test(Level world, ItemStack stack, boolean matchNBT) {
		if (isEmpty())
			return true;
		return FilterItem.testDirect(filterItemStack, stack, matchNBT);
	}

	public boolean test(Level world, FluidStack stack, boolean matchNBT) {
		if (isEmpty())
			return true;
		if (stack.isEmpty())
			return false;

		resolveFluid(world);

		if (filterFluidStack.isEmpty())
			return false;
		if (!matchNBT)
			return filterFluidStack.getFluid()
				.isSame(stack.getFluid());
		return filterFluidStack.isFluidEqual(stack);
	}

	//

	private void resolveFluid(Level world) {
		if (!fluidExtracted) {
			fluidExtracted = true;
			if (GenericItemEmptying.canItemBeEmptied(world, filterItemStack))
				filterFluidStack = GenericItemEmptying.emptyItem(world, filterItemStack, true)
				.getFirst();
		}
	}

	protected FilterItemStack(ItemStack filter) {
		filterItemStack = filter;
		filterFluidStack = FluidStack.EMPTY;
		fluidExtracted = false;
	}

	public static class ListFilterItemStack extends FilterItemStack {

		public List<FilterItemStack> containedItems;
		public boolean shouldRespectNBT;
		public boolean isBlacklist;

		protected ListFilterItemStack(ItemStack filter) {
			super(filter);

			containedItems = new ArrayList<>();
			ItemStackHandler items = FilterItem.getFilterItems(filter);
			for (int i = 0; i < items.getSlots().size(); i++) {
				ItemStack stackInSlot = items.getStackInSlot(i);
				if (!stackInSlot.isEmpty())
					containedItems.add(FilterItemStack.of(stackInSlot));
			}

			shouldRespectNBT = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
			isBlacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
		}

		@Override
		public boolean test(Level world, ItemStack stack, boolean matchNBT) {
			if (containedItems.isEmpty())
				return super.test(world, stack, matchNBT);
			for (FilterItemStack filterItemStack : containedItems)
				if (filterItemStack.test(world, stack, shouldRespectNBT))
					return !isBlacklist;
			return isBlacklist;
		}

		@Override
		public boolean test(Level world, FluidStack stack, boolean matchNBT) {
			for (FilterItemStack filterItemStack : containedItems)
				if (filterItemStack.test(world, stack, shouldRespectNBT))
					return !isBlacklist;
			return isBlacklist;
		}

	}

	public static class AttributeFilterItemStack extends FilterItemStack {

		public enum WhitelistMode {
			WHITELIST_DISJ, WHITELIST_CONJ, BLACKLIST;
		}

		public WhitelistMode whitelistMode;
		public List<Pair<ItemAttribute, Boolean>> attributeTests;

		protected AttributeFilterItemStack(ItemStack filter) {
			super(filter);
			boolean defaults = !filter.has(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE)
				&& !filter.has(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES)
				&& !filter.has(AllDataComponents.FILTER_DATA);

			attributeTests = new ArrayList<>();
			if (defaults) {
				whitelistMode = WhitelistMode.WHITELIST_DISJ;
			} else if (filter.has(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE)) {
				whitelistMode = WhitelistMode.values()[filter.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE).ordinal()];
			} else {
				whitelistMode = WhitelistMode.values()[filter.getOrDefault(AllDataComponents.FILTER_DATA, new CompoundTag())
					.getInt("WhitelistMode")];
			}

			List<CompoundTag> attributes = filter.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, List.of());
			if (attributes.isEmpty() && filter.has(AllDataComponents.FILTER_DATA)) {
				ListTag oldList = filter.get(AllDataComponents.FILTER_DATA).getList("MatchedAttributes", Tag.TAG_COMPOUND);
				attributes = oldList.stream().map(CompoundTag.class::cast).toList();
			}
			for (CompoundTag compound : attributes) {
				ItemAttribute attribute = ItemAttribute.fromNBT(compound);
				if (attribute != null)
					attributeTests.add(Pair.of(attribute, compound.getBoolean("Inverted")));
			}
		}

		@Override
		public boolean test(Level world, FluidStack stack, boolean matchNBT) {
			return false;
		}

		@Override
		public boolean test(Level world, ItemStack stack, boolean matchNBT) {
			if (attributeTests.isEmpty())
				return super.test(world, stack, matchNBT);
			for (Pair<ItemAttribute, Boolean> test : attributeTests) {
				ItemAttribute attribute = test.getFirst();
				boolean inverted = test.getSecond();
				boolean matches = attribute.appliesTo(stack, world) != inverted;

				if (matches) {
					switch (whitelistMode) {
					case BLACKLIST:
						return false;
					case WHITELIST_CONJ:
						continue;
					case WHITELIST_DISJ:
						return true;
					}
				} else {
					switch (whitelistMode) {
					case BLACKLIST:
						continue;
					case WHITELIST_CONJ:
						return false;
					case WHITELIST_DISJ:
						continue;
					}
				}
			}

			switch (whitelistMode) {
			case BLACKLIST:
				return true;
			case WHITELIST_CONJ:
				return true;
			case WHITELIST_DISJ:
				return false;
			}

			return false;
		}

	}

}
