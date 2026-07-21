package com.simibubi.create.compat.cobblemon;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CobblemonSeatPokemonPacket extends SimplePacketBase {

	private final BlockPos seatPos;
	private final boolean isPC;
	private final int boxIndex;
	private final int slotIndex;

	/** Party selection constructor. */
	public CobblemonSeatPokemonPacket(BlockPos seatPos, int partySlot) {
		this.seatPos = seatPos;
		this.isPC = false;
		this.boxIndex = 0;
		this.slotIndex = partySlot;
	}

	/** PC selection constructor. */
	public CobblemonSeatPokemonPacket(BlockPos seatPos, int boxIndex, int slotIndex) {
		this.seatPos = seatPos;
		this.isPC = true;
		this.boxIndex = boxIndex;
		this.slotIndex = slotIndex;
	}

	public CobblemonSeatPokemonPacket(RegistryFriendlyByteBuf buffer) {
		this.seatPos = buffer.readBlockPos();
		this.isPC = buffer.readBoolean();
		if (isPC) {
			this.boxIndex = buffer.readInt();
			this.slotIndex = buffer.readInt();
		} else {
			this.boxIndex = 0;
			this.slotIndex = buffer.readInt();
		}
	}

	@Override
	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(seatPos);
		buffer.writeBoolean(isPC);
		if (isPC) {
			buffer.writeInt(boxIndex);
		}
		buffer.writeInt(slotIndex);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;

			if (player.distanceToSqr(seatPos.getX() + 0.5, seatPos.getY() + 0.5, seatPos.getZ() + 0.5) > 20 * 20)
				return;

			if (!(player.level().getBlockState(seatPos).getBlock() instanceof SeatBlock))
				return;

			if (SeatBlock.isSeatOccupied(player.level(), seatPos))
				return;

			if (!Mods.COBBLEMON.isLoaded())
				return;

			if (isPC) {
				if (boxIndex < 0 || slotIndex < 0 || slotIndex > 29)
					return;
				CobblemonCompat.spawnAndSeatPokemonFromPC(player, seatPos, boxIndex, slotIndex);
			} else {
				if (slotIndex < 0 || slotIndex > 5)
					return;
				CobblemonCompat.spawnAndSeatPokemon(player, seatPos, slotIndex);
			}
		});
		return true;
	}
}
