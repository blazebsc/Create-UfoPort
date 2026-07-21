package com.simibubi.create.compat.cobblemon;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.cobblemon.CobblemonCompat.PartySlotData;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CobblemonSeatPartyDataPacket extends SimplePacketBase {

	private final BlockPos seatPos;
	private final List<PartySlotData> partyData;
	private final int pcBoxCount;

	public CobblemonSeatPartyDataPacket(BlockPos seatPos, List<PartySlotData> partyData, int pcBoxCount) {
		this.seatPos = seatPos;
		this.partyData = partyData;
		this.pcBoxCount = pcBoxCount;
	}

	public CobblemonSeatPartyDataPacket(RegistryFriendlyByteBuf buffer) {
		this.seatPos = buffer.readBlockPos();
		this.partyData = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			boolean present = buffer.readBoolean();
			if (present) {
				String speciesName = buffer.readUtf(64);
				int level = buffer.readInt();
				int aspectCount = buffer.readInt();
				List<String> aspects = new ArrayList<>();
				for (int j = 0; j < aspectCount; j++) {
					aspects.add(buffer.readUtf(128));
				}
				partyData.add(new PartySlotData(true, speciesName, level, aspects));
			} else {
				partyData.add(new PartySlotData(false, "", 0, List.of()));
			}
		}
		this.pcBoxCount = buffer.readInt();
	}

	@Override
	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(seatPos);
		for (int i = 0; i < 6; i++) {
			PartySlotData slot = i < partyData.size() ? partyData.get(i) : new PartySlotData(false, "", 0, List.of());
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
		buffer.writeInt(pcBoxCount);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> openScreen()));
		return true;
	}

	@Environment(EnvType.CLIENT)
	private void openScreen() {
		ScreenOpener.open(new CobblemonSeatScreen(seatPos, partyData, pcBoxCount));
	}
}
