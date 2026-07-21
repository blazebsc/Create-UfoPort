package com.simibubi.create.content.schematics.packet;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.SchematicInstances;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class SchematicSyncPacket extends SimplePacketBase {

	public int slot;
	public boolean deployed;
	public BlockPos anchor;
	public Rotation rotation;
	public Mirror mirror;

	public SchematicSyncPacket(int slot, StructurePlaceSettings settings,
			BlockPos anchor, boolean deployed) {
		this.slot = slot;
		this.deployed = deployed;
		this.anchor = anchor;
		this.rotation = settings.getRotation();
		this.mirror = settings.getMirror();
	}

	public SchematicSyncPacket(RegistryFriendlyByteBuf buffer) {
		slot = buffer.readVarInt();
		deployed = buffer.readBoolean();
		anchor = buffer.readBlockPos();
		rotation = buffer.readEnum(Rotation.class);
		mirror = buffer.readEnum(Mirror.class);
	}

	@Override
	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeVarInt(slot);
		buffer.writeBoolean(deployed);
		buffer.writeBlockPos(anchor);
		buffer.writeEnum(rotation);
		buffer.writeEnum(mirror);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			ItemStack stack = ItemStack.EMPTY;
			if (slot == -1) {
				stack = player.getMainHandItem();
			} else {
				stack = player.getInventory().getItem(slot);
			}
			if (!AllItems.SCHEMATIC.isIn(stack)) {
				return;
			}
			stack.set(AllDataComponents.SCHEMATIC_DEPLOYED, deployed);
			stack.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);
			stack.set(AllDataComponents.SCHEMATIC_ROTATION, rotation);
			stack.set(AllDataComponents.SCHEMATIC_MIRROR, mirror);
			SchematicInstances.clearHash(stack);
		});
		return true;
	}

}
