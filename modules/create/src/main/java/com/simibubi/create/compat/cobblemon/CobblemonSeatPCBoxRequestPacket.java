package com.simibubi.create.compat.cobblemon;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.cobblemon.CobblemonCompat.PartySlotData;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CobblemonSeatPCBoxRequestPacket extends SimplePacketBase {

	private final BlockPos seatPos;
	private final int boxIndex;

	public CobblemonSeatPCBoxRequestPacket(BlockPos seatPos, int boxIndex) {
		this.seatPos = seatPos;
		this.boxIndex = boxIndex;
	}

	public CobblemonSeatPCBoxRequestPacket(RegistryFriendlyByteBuf buffer) {
		this.seatPos = buffer.readBlockPos();
		this.boxIndex = buffer.readInt();
	}

	@Override
	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(seatPos);
		buffer.writeInt(boxIndex);
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

			if (!Mods.COBBLEMON.isLoaded())
				return;

			int totalBoxes = CobblemonCompat.getPCBoxCount(player);
			if (boxIndex < 0 || boxIndex >= totalBoxes)
				return;

			List<PartySlotData> boxData = CobblemonCompat.getPCBoxData(player, boxIndex);
			String boxName = CobblemonCompat.getPCBoxName(player, boxIndex);

			AllPackets.getChannel().sendToClient(
				new CobblemonSeatPCBoxDataPacket(seatPos, boxIndex, boxName, boxData),
				player);
		});
		return true;
	}
}
