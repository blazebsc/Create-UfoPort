package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Pair;

import io.netty.buffer.ByteBuf;
import io.github.fabricators_of_create.porting_lib_ufo.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib_ufo.transfer.item.SlotItemHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AttributeFilterMenu extends AbstractFilterMenu {

	public enum WhitelistMode {
		WHITELIST_DISJ, WHITELIST_CONJ, BLACKLIST;

		public static final Codec<WhitelistMode> CODEC =
			Codec.STRING.xmap(WhitelistMode::valueOf, WhitelistMode::name);
		public static final StreamCodec<ByteBuf, WhitelistMode> STREAM_CODEC =
			ByteBufCodecs.VAR_INT.map(i -> WhitelistMode.values()[i], Enum::ordinal);
	}

	WhitelistMode whitelistMode;
	List<Pair<ItemAttribute, Boolean>> selectedAttributes;

	public AttributeFilterMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public AttributeFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack stack) {
		super(type, id, inv, stack);
	}

	public static AttributeFilterMenu create(int id, Inventory inv, ItemStack stack) {
		return new AttributeFilterMenu(AllMenuTypes.ATTRIBUTE_FILTER.get(), id, inv, stack);
	}

	public void appendSelectedAttribute(ItemAttribute itemAttribute, boolean inverted) {
		selectedAttributes.add(Pair.of(itemAttribute, inverted));
	}

	@Override
	protected void init(Inventory inv, ItemStack contentHolder) {
		super.init(inv, contentHolder);
		ItemStack stack = new ItemStack(Items.NAME_TAG);
		stack.set(DataComponents.CUSTOM_NAME,
				Components.literal("Selected Tags").withStyle(ChatFormatting.RESET, ChatFormatting.BLUE));
		ghostInventory.setStackInSlot(1, stack);
	}

	@Override
	protected int getPlayerInventoryXOffset() {
		return 51;
	}

	@Override
	protected int getPlayerInventoryYOffset() {
		return 107;
	}

	@Override
	protected void addFilterSlots() {
		this.addSlot(new SlotItemHandler(ghostInventory, 0, 16, 24));
		this.addSlot(new SlotItemHandler(ghostInventory, 1, 22, 59) {
			@Override
			public boolean mayPickup(Player playerIn) {
				return false;
			}
		});
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler(2);
	}

	@Override
	public void clearContents() {
		selectedAttributes.clear();
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		if (slotId == 37)
			return;
		super.clicked(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public boolean canDragTo(Slot slotIn) {
		if (slotIn.index == 37)
			return false;
		return super.canDragTo(slotIn);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
		if (slotIn.index == 37)
			return false;
		return super.canTakeItemForPickAll(stack, slotIn);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		if (index == 37)
			return ItemStack.EMPTY;
		if (index == 36) {
			ghostInventory.setStackInSlot(37, ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getItem(index);
			ItemStack copy = stackToInsert.copy();
			copy.setCount(1);
			ghostInventory.setStackInSlot(0, copy);
		}
		return ItemStack.EMPTY;
	}

	@Override
	protected void initAndReadInventory(ItemStack filterItem) {
		super.initAndReadInventory(filterItem);
		selectedAttributes = new ArrayList<>();
		whitelistMode = filterItem.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE,
			WhitelistMode.values()[filterItem.getOrDefault(AllDataComponents.FILTER_DATA, new CompoundTag())
				.getInt("WhitelistMode")]);
		List<CompoundTag> attributes = filterItem.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, List.of());
		if (attributes.isEmpty() && filterItem.has(AllDataComponents.FILTER_DATA)) {
			ListTag oldAttributes = filterItem.getOrDefault(AllDataComponents.FILTER_DATA, new CompoundTag())
				.getList("MatchedAttributes", Tag.TAG_COMPOUND);
			attributes = oldAttributes.stream().map(CompoundTag.class::cast).toList();
		}
		attributes.forEach(compound -> {
			selectedAttributes.add(Pair.of(ItemAttribute.fromNBT(compound), compound.getBoolean("Inverted")));
		});
	}

	@Override
	protected void saveData(ItemStack filterItem) {
		List<CompoundTag> attributes = new ArrayList<>();
		selectedAttributes.forEach(at -> {
			if (at == null)
				return;
			CompoundTag compoundNBT = new CompoundTag();
			at.getFirst()
					.serializeNBT(compoundNBT);
			compoundNBT.putBoolean("Inverted", at.getSecond());
			attributes.add(compoundNBT);
		});
		filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, whitelistMode);
		filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, attributes);
		filterItem.remove(AllDataComponents.FILTER_DATA);
		
		if (attributes.isEmpty() && whitelistMode == WhitelistMode.WHITELIST_DISJ)
			filterItem.remove(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES);
	}

}
