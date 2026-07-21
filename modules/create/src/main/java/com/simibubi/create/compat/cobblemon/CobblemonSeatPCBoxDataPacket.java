package com.simibubi.create.compat.cobblemon;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.cobblemon.CobblemonCompat.PartySlotData;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CobblemonSeatPCBoxDataPacket extends SimplePacketBase {

	private final BlockPos seatPos;
	private final int boxIndex;
	private final String boxName;
	private final List<PartySlotData> boxData;

	public CobblemonSeatPCBoxDataPacket(BlockPos seatPos, int boxIndex, String boxName, List<PartySlotData> boxData) {
		this.seatPos = seatPos;
		this.boxIndex = boxIndex;
		this.boxName = boxName;
		this.boxData = boxData;
	}

	public CobblemonSeatPCBoxDataPacket(RegistryFriendlyByteBuf buffer) {
		this.seatPos = buffer.readBlockPos();
		this.boxIndex = buffer.readInt();
		this.boxName = buffer.readUtf(64);
		int slotCount = buffer.readInt();
		this.boxData = new ArrayList<>();
		for (int i = 0; i < slotCount; i++) {
			boolean present = buffer.readBoolean();
			if (present) {
				String speciesName = buffer.readUtf(64);
				int level = buffer.readInt();
				int aspectCount = buffer.readInt();
				List<String> aspects = new ArrayList<>();
				for (int j = 0; j < aspectCount; j++) {
					aspects.add(buffer.readUtf(128));
				}
				boxData.add(new PartySlotData(true, speciesName, level, aspects));
			} else {
				boxData.add(new PartySlotData(false, "", 0, List.of()));
			}
		}
	}

	@Override
	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(seatPos);
		buffer.writeInt(boxIndex);
		buffer.writeUtf(boxName, 64);
		buffer.writeInt(boxData.size());
		for (PartySlotData slot : boxData) {
			buffer.writeBoolean(slot.present());
			if (slot.present()) {
				buffer.writeUtf(slot.speciesName(), 64);
				buffer.writeInt(slot.level());
				buffer.writeInt(slot.aspects().size());
				for (String aspect : slot.aspects()) {
					buffer.writeUtf(aspect, 128);
				}
			}
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> handleClient()));
		return true;
	}

	@Environment(EnvType.CLIENT)
	private void handleClient() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof CobblemonSeatScreen screen) {
			screen.receivePCBoxData(boxIndex, boxName, boxData);
		}
	}
}
