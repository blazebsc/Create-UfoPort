package com.simibubi.create.content.redstone.link.controller;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import io.github.fabricators_of_create.porting_lib_ufo.transfer.item.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LinkedControllerBindPacket extends LinkedControllerPacketBase {

	private int button;
	private BlockPos linkLocation;

	public LinkedControllerBindPacket(int button, BlockPos linkLocation) {
		super((BlockPos) null);
		this.button = button;
		this.linkLocation = linkLocation;
	}

	public LinkedControllerBindPacket(RegistryFriendlyByteBuf buffer) {
		super(buffer);
		this.button = buffer.readVarInt();
		this.linkLocation = buffer.readBlockPos();
	}

	@Override
	public void write(RegistryFriendlyByteBuf buffer) {
		super.write(buffer);
		buffer.writeVarInt(button);
		buffer.writeBlockPos(linkLocation);
	}

	@Override
	protected void handleItem(ServerPlayer player, ItemStack heldItem) {
		if (player.isSpectator())
			return;

		ItemStackHandler frequencyItems = LinkedControllerItem.getFrequencyItems(heldItem);
		LinkBehaviour linkBehaviour = BlockEntityBehaviour.get(player.level(), linkLocation, LinkBehaviour.TYPE);
		if (linkBehaviour == null)
			return;

		linkBehaviour.getNetworkKey()
			.forEachWithContext((f, first) -> frequencyItems.setStackInSlot(button * 2 + (first ? 0 : 1), f.getStack()
				.copy()));

		heldItem.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemHelper.containerContentsFromHandler(frequencyItems));
		heldItem.remove(AllDataComponents.FILTER_DATA);
	}

	@Override
	protected void handleLectern(ServerPlayer player, LecternControllerBlockEntity lectern) {}

}
