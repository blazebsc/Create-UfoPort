package io.github.fabricators_of_create.porting_lib_ufo.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BundleStorage implements Storage<ItemVariant> {
	private final ContainerItemContext context;
	public final Item item;

	public BundleStorage(Item item, ContainerItemContext context) {
		this.item = item;
		this.context = context;
	}

	private BundleContents getContents() {
		return context.getItemVariant().toStack()
				.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
	}

	private boolean setContents(BundleContents contents, TransactionContext tx) {
		ItemStack newStack = context.getItemVariant().toStack();
		newStack.set(DataComponents.BUNDLE_CONTENTS, contents);
		return context.exchange(ItemVariant.of(newStack), 1, tx) == 1;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (context.getAmount() != 1) return 0;
		if (resource.isBlank()) return 0;
		if (!resource.getItem().canFitInsideContainerItems()) return 0;
		if (maxAmount <= 0) return 0;

		BundleContents contents = getContents();
		long totalInserted = 0;

		int toInsert = (int) Math.min(maxAmount, resource.getItem().getDefaultMaxStackSize());

		while (totalInserted < maxAmount) {
			int batchSize = (int) Math.min(toInsert, maxAmount - totalInserted);
			if (batchSize <= 0) break;

			ItemStack stackToInsert = resource.toStack(batchSize);
			BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
			int leftover = mutable.tryInsert(stackToInsert);
			int inserted = batchSize - leftover;

			if (inserted <= 0) break;

			contents = mutable.toImmutable();
			totalInserted += inserted;
		}

		if (totalInserted > 0) {
			if (!setContents(contents, transaction)) {
				return 0;
			}
		}

		return totalInserted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (context.getAmount() != 1) return 0;
		if (resource.isBlank()) return 0;
		if (maxAmount <= 0) return 0;

		BundleContents contents = getContents();
		List<ItemStack> items = new ArrayList<>();
		contents.itemCopyStream().forEach(items::add);

		long totalExtracted = 0;
		List<ItemStack> remaining = new ArrayList<>();

		for (ItemStack stack : items) {
			if (totalExtracted >= maxAmount) {
				remaining.add(stack);
				continue;
			}

			if (ItemVariant.of(stack).equals(resource)) {
				long canExtract = Math.min(stack.getCount(), maxAmount - totalExtracted);
				totalExtracted += canExtract;
				int leftInStack = stack.getCount() - (int) canExtract;
				if (leftInStack > 0) {
					ItemStack leftover = stack.copy();
					leftover.setCount(leftInStack);
					remaining.add(leftover);
				}
			} else {
				remaining.add(stack);
			}
		}

		if (totalExtracted > 0) {
			BundleContents.Mutable mutable = new BundleContents.Mutable(BundleContents.EMPTY);
			for (ItemStack stack : remaining) {
				mutable.tryInsert(stack);
			}
			BundleContents newContents = mutable.toImmutable();

			if (!setContents(newContents, transaction)) {
				return 0;
			}
		}

		return totalExtracted;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		BundleContents contents = getContents();
		List<ItemStack> items = new ArrayList<>();
		contents.itemCopyStream().forEach(items::add);

		List<StorageView<ItemVariant>> views = new ArrayList<>();
		for (ItemStack stack : items) {
			views.add(new BundleSlotView(stack));
		}
		return views.iterator();
	}

	private class BundleSlotView implements StorageView<ItemVariant> {
		private final ItemStack stack;

		BundleSlotView(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return BundleStorage.this.extract(resource, maxAmount, transaction);
		}

		@Override
		public boolean isResourceBlank() {
			return stack.isEmpty();
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
			return stack.getMaxStackSize();
		}
	}
}
