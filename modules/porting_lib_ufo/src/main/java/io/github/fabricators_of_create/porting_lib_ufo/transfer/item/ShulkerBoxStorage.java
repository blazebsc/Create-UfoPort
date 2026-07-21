package io.github.fabricators_of_create.porting_lib_ufo.transfer.item;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class ShulkerBoxStorage implements Storage<ItemVariant> {
	private static final int SHULKER_SLOTS = 27;

	private final ContainerItemContext context;
	public final Item item;

	public ShulkerBoxStorage(Item item, ContainerItemContext context) {
		this.item = item;
		this.context = context;
	}

	private NonNullList<ItemStack> getContents() {
		NonNullList<ItemStack> items = NonNullList.withSize(SHULKER_SLOTS, ItemStack.EMPTY);
		ItemContainerContents contents = context.getItemVariant().toStack()
				.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		contents.copyInto(items);
		return items;
	}

	private boolean setContents(NonNullList<ItemStack> contents, TransactionContext tx) {
		ItemStack newStack = context.getItemVariant().toStack();
		newStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
		return context.exchange(ItemVariant.of(newStack), 1, tx) == 1;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (context.getAmount() != 1 || resource.isBlank() || maxAmount <= 0) {
			return 0;
		}

		if (!resource.getItem().canFitInsideContainerItems()) {
			return 0;
		}

		NonNullList<ItemStack> contents = getContents();
		int maxStackSize = resource.getItem().getDefaultMaxStackSize();
		long remaining = maxAmount;

		for (int i = 0; i < SHULKER_SLOTS && remaining > 0; i++) {
			ItemStack slot = contents.get(i);
			if (!slot.isEmpty() && ItemVariant.of(slot).equals(resource)) {
				int space = maxStackSize - slot.getCount();
				if (space > 0) {
					int toInsert = (int) Math.min(space, remaining);
					slot.grow(toInsert);
					remaining -= toInsert;
				}
			}
		}

		for (int i = 0; i < SHULKER_SLOTS && remaining > 0; i++) {
			ItemStack slot = contents.get(i);
			if (slot.isEmpty()) {
				int toInsert = (int) Math.min(maxStackSize, remaining);
				contents.set(i, resource.toStack(toInsert));
				remaining -= toInsert;
			}
		}

		long inserted = maxAmount - remaining;
		if (inserted > 0) {
			if (setContents(contents, transaction)) {
				return inserted;
			}
		}

		return 0;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (context.getAmount() != 1 || resource.isBlank() || maxAmount <= 0) {
			return 0;
		}

		NonNullList<ItemStack> contents = getContents();
		long remaining = maxAmount;

		for (int i = 0; i < SHULKER_SLOTS && remaining > 0; i++) {
			ItemStack slot = contents.get(i);
			if (!slot.isEmpty() && ItemVariant.of(slot).equals(resource)) {
				int toExtract = (int) Math.min(slot.getCount(), remaining);
				slot.shrink(toExtract);
				if (slot.isEmpty()) {
					contents.set(i, ItemStack.EMPTY);
				}
				remaining -= toExtract;
			}
		}

		long extracted = maxAmount - remaining;
		if (extracted > 0) {
			if (setContents(contents, transaction)) {
				return extracted;
			}
		}

		return 0;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		NonNullList<ItemStack> contents = getContents();
		return new Iterator<>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return index < SHULKER_SLOTS;
			}

			@Override
			public StorageView<ItemVariant> next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return new ShulkerSlotView(contents.get(index++));
			}
		};
	}

	private class ShulkerSlotView implements StorageView<ItemVariant> {
		private final ItemStack stack;

		ShulkerSlotView(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(stack);
		}

		@Override
		public long getAmount() {
			return stack.getCount();
		}

		@Override
		public long getCapacity() {
			return stack.isEmpty() ? 64 : stack.getMaxStackSize();
		}

		@Override
		public boolean isResourceBlank() {
			return stack.isEmpty();
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return ShulkerBoxStorage.this.extract(resource, maxAmount, transaction);
		}
	}
}
