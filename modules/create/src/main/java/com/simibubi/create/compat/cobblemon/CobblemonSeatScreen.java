package com.simibubi.create.compat.cobblemon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.compat.cobblemon.CobblemonCompat.PartySlotData;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.utility.Lang;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class CobblemonSeatScreen extends AbstractSimiScreen {

	private static final int SLOT_WIDTH = 200;
	private static final int SLOT_HEIGHT = 36;
	private static final int SLOT_GAP = 2;
	private static final int PADDING = 8;
	private static final int TITLE_HEIGHT = 18;
	private static final int PORTRAIT_SIZE = 32;
	private static final int TAB_HEIGHT = 18;
	private static final int TAB_GAP = 2;
	private static final int NAV_BAR_HEIGHT = 20;
	private static final int MAX_VISIBLE_SLOTS = 6;

	// Cobblemon grayscale UI palette
	private static final int BG_COLOR = 0xE62F2F2F;
	private static final int BG_BORDER = 0xFF4B4B4B;
	private static final int HEADER_COLOR = 0xFF676767;
	private static final int HEADER_TEXT = 0xFFFFFFFF;

	private static final int SLOT_COLOR = 0xFF3D3D3D;
	private static final int SLOT_HOVER_COLOR = 0xFF4B4B4B;
	private static final int SLOT_BORDER = 0xFF4B4B4B;
	private static final int SLOT_HOVER_BORDER = 0xFF8D8D8D;

	private static final int PORTRAIT_BG = 0xFF2F2F2F;

	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int LEVEL_COLOR = 0xFFC6C6C6;

	private static final int TAB_ACTIVE_BG = 0xFF8D8D8D;
	private static final int TAB_ACTIVE_TEXT = 0xFFFFFFFF;
	private static final int TAB_INACTIVE_BG = 0xFF3D3D3D;
	private static final int TAB_INACTIVE_TEXT = 0xFFA0A0A0;
	private static final int TAB_DISABLED_BG = 0xFF2F2F2F;
	private static final int TAB_DISABLED_TEXT = 0xFF595959;
	private static final int TAB_HOVER_BG = 0xFF595959;

	private static final int NAV_BUTTON_BG = 0xFF3D3D3D;
	private static final int NAV_BUTTON_HOVER = 0xFF595959;
	private static final int NAV_BUTTON_TEXT = 0xFFC6C6C6;
	private static final int NAV_NAME_TEXT = 0xFFFFFFFF;

	private static final int LOADING_COLOR = 0xFFA0A0A0;
	private static final int SCROLL_INFO_COLOR = 0xFFA0A0A0;

	private enum Tab { PARTY, PC }

	private final BlockPos seatPos;
	private final List<PartySlotData> partyData;
	private final int pcBoxCount;

	private Tab activeTab = Tab.PARTY;

	// Party portrait state
	private Object[] partyPortraitStates;
	private float[] partyBaseScales;
	private boolean portraitsAvailable;
	private boolean spritesAvailable;

	// PC state
	private int currentBoxIndex = 0;
	private String currentBoxName = "";
	private List<PartySlotData> currentBoxData;
	private Object[] pcPortraitStates;
	private float[] pcBaseScales;
	private boolean pcLoading = false;
	private int pcScrollOffset = 0;

	public CobblemonSeatScreen(BlockPos seatPos, List<PartySlotData> partyData, int pcBoxCount) {
		super(Lang.translateDirect("pokemon_seat.title"));
		this.seatPos = seatPos;
		this.partyData = partyData;
		this.pcBoxCount = pcBoxCount;
	}

	@Override
	protected void init() {
		int contentHeight = computeWindowHeight();
		setWindowSize(SLOT_WIDTH + PADDING * 2, contentHeight);
		super.init();

		portraitsAvailable = CobblemonPortraitRenderer.isAvailable();
		spritesAvailable = CobblemonPortraitRenderer.isSpriteAvailable();
		initPartyPortraits();
	}

	private void initPartyPortraits() {
		partyPortraitStates = new Object[partyData.size()];
		partyBaseScales = new float[partyData.size()];

		if (portraitsAvailable) {
			for (int i = 0; i < partyData.size(); i++) {
				PartySlotData slot = partyData.get(i);
				if (slot.present()) {
					partyPortraitStates[i] = CobblemonPortraitRenderer.createState(
							new HashSet<>(slot.aspects()));
					partyBaseScales[i] = CobblemonPortraitRenderer.getBaseScale(slot.speciesName());
				}
			}
		}
	}

	private void initPCPortraits() {
		if (currentBoxData == null)
			return;
		pcPortraitStates = new Object[currentBoxData.size()];
		pcBaseScales = new float[currentBoxData.size()];

		if (portraitsAvailable) {
			for (int i = 0; i < currentBoxData.size(); i++) {
				PartySlotData slot = currentBoxData.get(i);
				if (slot.present()) {
					pcPortraitStates[i] = CobblemonPortraitRenderer.createState(
							new HashSet<>(slot.aspects()));
					pcBaseScales[i] = CobblemonPortraitRenderer.getBaseScale(slot.speciesName());
				}
			}
		}
	}

	private int computeWindowHeight() {
		int tabSection = TAB_HEIGHT + TAB_GAP;
		int slotsHeight;

		if (activeTab == Tab.PARTY) {
			int presentCount = (int) partyData.stream().filter(PartySlotData::present).count();
			slotsHeight = Math.max(1, presentCount) * (SLOT_HEIGHT + SLOT_GAP) - SLOT_GAP;
		} else {
			slotsHeight = NAV_BAR_HEIGHT + SLOT_GAP
				+ MAX_VISIBLE_SLOTS * (SLOT_HEIGHT + SLOT_GAP) - SLOT_GAP;
		}

		return TITLE_HEIGHT + tabSection + slotsHeight + PADDING * 2;
	}

	public void receivePCBoxData(int boxIndex, String boxName, List<PartySlotData> boxData) {
		if (boxIndex != currentBoxIndex)
			return;
		this.currentBoxName = boxName;
		this.currentBoxData = boxData;
		this.pcLoading = false;
		this.pcScrollOffset = 0;
		initPCPortraits();
	}

	private void requestPCBox(int boxIndex) {
		this.currentBoxIndex = boxIndex;
		this.currentBoxData = null;
		this.pcPortraitStates = null;
		this.pcBaseScales = null;
		this.pcLoading = true;
		this.pcScrollOffset = 0;
		AllPackets.getChannel().sendToServer(new CobblemonSeatPCBoxRequestPacket(seatPos, boxIndex));
	}

	private void switchTab(Tab tab) {
		if (tab == activeTab)
			return;
		if (tab == Tab.PC && pcBoxCount <= 0)
			return;
		activeTab = tab;

		int contentHeight = computeWindowHeight();
		setWindowSize(SLOT_WIDTH + PADDING * 2, contentHeight);
		super.init();
		initPartyPortraits();

		if (tab == Tab.PC && currentBoxData == null && !pcLoading) {
			requestPCBox(0);
		}
	}

	// --- Rendering ---

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int contentHeight = computeWindowHeight();

		// Main background
		graphics.fill(guiLeft, guiTop, guiLeft + windowWidth, guiTop + contentHeight, BG_COLOR);
		renderBorder(graphics, guiLeft, guiTop, windowWidth, contentHeight, BG_BORDER);

		// Title bar
		graphics.fill(guiLeft, guiTop, guiLeft + windowWidth, guiTop + TITLE_HEIGHT + PADDING, HEADER_COLOR);
		graphics.drawCenteredString(font, title, guiLeft + windowWidth / 2, guiTop + PADDING, HEADER_TEXT);

		int y = guiTop + PADDING + TITLE_HEIGHT;

		// Tabs
		y = renderTabs(graphics, mouseX, mouseY, y);
		y += TAB_GAP;

		// Content
		if (activeTab == Tab.PARTY) {
			renderPartyContent(graphics, mouseX, mouseY, y, partialTicks);
		} else {
			renderPCContent(graphics, mouseX, mouseY, y, partialTicks);
		}
	}

	private int renderTabs(GuiGraphics graphics, int mouseX, int mouseY, int y) {
		int tabWidth = (SLOT_WIDTH - TAB_GAP) / 2;
		int partyTabX = guiLeft + PADDING;
		int pcTabX = partyTabX + tabWidth + TAB_GAP;

		// Party tab
		boolean partyActive = activeTab == Tab.PARTY;
		boolean partyHovered = !partyActive && mouseX >= partyTabX && mouseX < partyTabX + tabWidth
				&& mouseY >= y && mouseY < y + TAB_HEIGHT;
		int partyBg = partyActive ? TAB_ACTIVE_BG : (partyHovered ? TAB_HOVER_BG : TAB_INACTIVE_BG);
		int partyText = partyActive ? TAB_ACTIVE_TEXT : TAB_INACTIVE_TEXT;
		graphics.fill(partyTabX, y, partyTabX + tabWidth, y + TAB_HEIGHT, partyBg);
		if (partyActive)
			renderBorder(graphics, partyTabX, y, tabWidth, TAB_HEIGHT, HEADER_COLOR);
		graphics.drawCenteredString(font, "Party", partyTabX + tabWidth / 2, y + (TAB_HEIGHT - 8) / 2, partyText);

		// PC tab
		boolean pcDisabled = pcBoxCount <= 0;
		boolean pcActive = activeTab == Tab.PC;
		boolean pcHovered = !pcDisabled && !pcActive && mouseX >= pcTabX && mouseX < pcTabX + tabWidth
				&& mouseY >= y && mouseY < y + TAB_HEIGHT;
		int pcBg = pcDisabled ? TAB_DISABLED_BG : (pcActive ? TAB_ACTIVE_BG : (pcHovered ? TAB_HOVER_BG : TAB_INACTIVE_BG));
		int pcText = pcDisabled ? TAB_DISABLED_TEXT : (pcActive ? TAB_ACTIVE_TEXT : TAB_INACTIVE_TEXT);
		graphics.fill(pcTabX, y, pcTabX + tabWidth, y + TAB_HEIGHT, pcBg);
		if (pcActive && !pcDisabled)
			renderBorder(graphics, pcTabX, y, tabWidth, TAB_HEIGHT, HEADER_COLOR);
		graphics.drawCenteredString(font, "PC", pcTabX + tabWidth / 2, y + (TAB_HEIGHT - 8) / 2, pcText);

		return y + TAB_HEIGHT;
	}

	private void renderPartyContent(GuiGraphics graphics, int mouseX, int mouseY, int startY, float partialTicks) {
		int slotY = startY;
		for (int i = 0; i < partyData.size(); i++) {
			PartySlotData slot = partyData.get(i);
			if (!slot.present())
				continue;

			renderPokemonSlot(graphics, mouseX, mouseY, slotY, slot,
				partyPortraitStates != null && i < partyPortraitStates.length ? partyPortraitStates[i] : null,
				partyBaseScales != null && i < partyBaseScales.length ? partyBaseScales[i] : 1.0f,
				partialTicks);
			slotY += SLOT_HEIGHT + SLOT_GAP;
		}
	}

	private void renderPCContent(GuiGraphics graphics, int mouseX, int mouseY, int startY, float partialTicks) {
		renderNavBar(graphics, mouseX, mouseY, startY);
		int slotStartY = startY + NAV_BAR_HEIGHT + SLOT_GAP;

		if (pcLoading) {
			graphics.drawCenteredString(font, "Loading...",
				guiLeft + windowWidth / 2, slotStartY + MAX_VISIBLE_SLOTS * (SLOT_HEIGHT + SLOT_GAP) / 2 - 4,
				LOADING_COLOR);
			return;
		}

		if (currentBoxData == null)
			return;

		List<Integer> presentIndices = new ArrayList<>();
		for (int i = 0; i < currentBoxData.size(); i++) {
			if (currentBoxData.get(i).present())
				presentIndices.add(i);
		}

		if (presentIndices.isEmpty()) {
			graphics.drawCenteredString(font, "Empty",
				guiLeft + windowWidth / 2, slotStartY + MAX_VISIBLE_SLOTS * (SLOT_HEIGHT + SLOT_GAP) / 2 - 4,
				LOADING_COLOR);
			return;
		}

		int maxScroll = Math.max(0, presentIndices.size() - MAX_VISIBLE_SLOTS);
		pcScrollOffset = Math.min(pcScrollOffset, maxScroll);

		int slotY = slotStartY;
		int visibleCount = 0;
		for (int vi = pcScrollOffset; vi < presentIndices.size() && visibleCount < MAX_VISIBLE_SLOTS; vi++) {
			int dataIndex = presentIndices.get(vi);
			PartySlotData slot = currentBoxData.get(dataIndex);

			renderPokemonSlot(graphics, mouseX, mouseY, slotY, slot,
				pcPortraitStates != null && dataIndex < pcPortraitStates.length ? pcPortraitStates[dataIndex] : null,
				pcBaseScales != null && dataIndex < pcBaseScales.length ? pcBaseScales[dataIndex] : 1.0f,
				partialTicks);
			slotY += SLOT_HEIGHT + SLOT_GAP;
			visibleCount++;
		}

		if (presentIndices.size() > MAX_VISIBLE_SLOTS) {
			String scrollInfo = (pcScrollOffset + 1) + "-" + Math.min(pcScrollOffset + MAX_VISIBLE_SLOTS, presentIndices.size())
				+ " / " + presentIndices.size();
			graphics.drawCenteredString(font, scrollInfo,
				guiLeft + windowWidth / 2, slotY + 2, SCROLL_INFO_COLOR);
		}
	}

	private void renderNavBar(GuiGraphics graphics, int mouseX, int mouseY, int y) {
		int navX = guiLeft + PADDING;
		int buttonSize = NAV_BAR_HEIGHT;

		// Background bar
		graphics.fill(navX, y, navX + SLOT_WIDTH, y + buttonSize, SLOT_COLOR);

		// Left arrow
		boolean leftHovered = mouseX >= navX && mouseX < navX + buttonSize
				&& mouseY >= y && mouseY < y + buttonSize;
		graphics.fill(navX, y, navX + buttonSize, y + buttonSize,
			leftHovered ? NAV_BUTTON_HOVER : NAV_BUTTON_BG);
		if (leftHovered)
			renderBorder(graphics, navX, y, buttonSize, buttonSize, HEADER_COLOR);
		graphics.drawCenteredString(font, "<", navX + buttonSize / 2, y + (buttonSize - 8) / 2, NAV_BUTTON_TEXT);

		// Right arrow
		int rightX = navX + SLOT_WIDTH - buttonSize;
		boolean rightHovered = mouseX >= rightX && mouseX < rightX + buttonSize
				&& mouseY >= y && mouseY < y + buttonSize;
		graphics.fill(rightX, y, rightX + buttonSize, y + buttonSize,
			rightHovered ? NAV_BUTTON_HOVER : NAV_BUTTON_BG);
		if (rightHovered)
			renderBorder(graphics, rightX, y, buttonSize, buttonSize, HEADER_COLOR);
		graphics.drawCenteredString(font, ">", rightX + buttonSize / 2, y + (buttonSize - 8) / 2, NAV_BUTTON_TEXT);

		// Box name
		String displayName = currentBoxName.isEmpty() ? "Box " + (currentBoxIndex + 1) : currentBoxName;
		graphics.drawCenteredString(font, displayName,
			guiLeft + windowWidth / 2, y + (buttonSize - 8) / 2, NAV_NAME_TEXT);
	}

	private void renderPokemonSlot(GuiGraphics graphics, int mouseX, int mouseY, int slotY,
			PartySlotData slot, Object portraitState, float baseScale, float partialTicks) {
		int slotX = guiLeft + PADDING;
		boolean hovered = mouseX >= slotX && mouseX < slotX + SLOT_WIDTH
				&& mouseY >= slotY && mouseY < slotY + SLOT_HEIGHT;

		int bgColor = hovered ? SLOT_HOVER_COLOR : SLOT_COLOR;
		graphics.fill(slotX, slotY, slotX + SLOT_WIDTH, slotY + SLOT_HEIGHT, bgColor);

		if (hovered)
			renderBorder(graphics, slotX, slotY, SLOT_WIDTH, SLOT_HEIGHT, SLOT_HOVER_BORDER);
		else
			renderBorder(graphics, slotX, slotY, SLOT_WIDTH, SLOT_HEIGHT, SLOT_BORDER);

		// Portrait area
		int portraitX = slotX + 2;
		int portraitY = slotY + 2;
		graphics.fill(portraitX, portraitY,
				portraitX + PORTRAIT_SIZE, portraitY + PORTRAIT_SIZE, PORTRAIT_BG);

		// Try sprite first, fall back to 3D portrait
		if (portraitsAvailable && portraitState != null) {
			boolean spriteRendered = false;
			if (spritesAvailable) {
				spriteRendered = CobblemonPortraitRenderer.renderSprite(
						graphics, slot.speciesName(), portraitState,
						portraitX, portraitY, PORTRAIT_SIZE);
			}
			if (!spriteRendered) {
				graphics.enableScissor(portraitX, portraitY,
						portraitX + PORTRAIT_SIZE, portraitY + PORTRAIT_SIZE);
				CobblemonPortraitRenderer.renderPortrait(
						graphics, slot.speciesName(), portraitState,
						portraitX, portraitY, PORTRAIT_SIZE,
						baseScale, 0F);
				graphics.disableScissor();
			}
		}

		// Pokemon name and level
		int textX = slotX + PORTRAIT_SIZE + 10;
		String name = capitalize(slot.speciesName());
		String level = "Lv." + slot.level();

		graphics.drawString(font, name, textX, slotY + (SLOT_HEIGHT - 8) / 2, TEXT_COLOR);
		int levelWidth = font.width(level);
		graphics.drawString(font, level, slotX + SLOT_WIDTH - 8 - levelWidth,
				slotY + (SLOT_HEIGHT - 8) / 2, LEVEL_COLOR);
	}

	// --- Input Handling ---

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Tab clicks
		int tabY = guiTop + PADDING + TITLE_HEIGHT;
		int tabWidth = (SLOT_WIDTH - TAB_GAP) / 2;
		int partyTabX = guiLeft + PADDING;
		int pcTabX = partyTabX + tabWidth + TAB_GAP;

		if (mouseY >= tabY && mouseY < tabY + TAB_HEIGHT) {
			if (mouseX >= partyTabX && mouseX < partyTabX + tabWidth) {
				switchTab(Tab.PARTY);
				return true;
			}
			if (mouseX >= pcTabX && mouseX < pcTabX + tabWidth) {
				switchTab(Tab.PC);
				return true;
			}
		}

		if (activeTab == Tab.PARTY) {
			return handlePartyClick(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
		} else {
			return handlePCClick(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
		}
	}

	private boolean handlePartyClick(double mouseX, double mouseY) {
		int slotY = guiTop + PADDING + TITLE_HEIGHT + TAB_HEIGHT + TAB_GAP;
		for (int i = 0; i < partyData.size(); i++) {
			PartySlotData slot = partyData.get(i);
			if (!slot.present())
				continue;

			int slotX = guiLeft + PADDING;
			if (mouseX >= slotX && mouseX < slotX + SLOT_WIDTH
					&& mouseY >= slotY && mouseY < slotY + SLOT_HEIGHT) {
				AllPackets.getChannel().sendToServer(new CobblemonSeatPokemonPacket(seatPos, i));
				onClose();
				return true;
			}

			slotY += SLOT_HEIGHT + SLOT_GAP;
		}
		return false;
	}

	private boolean handlePCClick(double mouseX, double mouseY) {
		int navY = guiTop + PADDING + TITLE_HEIGHT + TAB_HEIGHT + TAB_GAP;
		int navX = guiLeft + PADDING;
		int buttonSize = NAV_BAR_HEIGHT;

		// Left arrow
		if (mouseX >= navX && mouseX < navX + buttonSize
				&& mouseY >= navY && mouseY < navY + buttonSize) {
			int newIndex = (currentBoxIndex - 1 + pcBoxCount) % pcBoxCount;
			requestPCBox(newIndex);
			return true;
		}

		// Right arrow
		int rightX = navX + SLOT_WIDTH - buttonSize;
		if (mouseX >= rightX && mouseX < rightX + buttonSize
				&& mouseY >= navY && mouseY < navY + buttonSize) {
			int newIndex = (currentBoxIndex + 1) % pcBoxCount;
			requestPCBox(newIndex);
			return true;
		}

		// Pokemon slot clicks
		if (currentBoxData == null || pcLoading)
			return false;

		List<Integer> presentIndices = new ArrayList<>();
		for (int i = 0; i < currentBoxData.size(); i++) {
			if (currentBoxData.get(i).present())
				presentIndices.add(i);
		}

		int slotStartY = navY + NAV_BAR_HEIGHT + SLOT_GAP;
		int slotY = slotStartY;
		int visibleCount = 0;
		for (int vi = pcScrollOffset; vi < presentIndices.size() && visibleCount < MAX_VISIBLE_SLOTS; vi++) {
			int dataIndex = presentIndices.get(vi);
			int slotX = guiLeft + PADDING;
			if (mouseX >= slotX && mouseX < slotX + SLOT_WIDTH
					&& mouseY >= slotY && mouseY < slotY + SLOT_HEIGHT) {
				AllPackets.getChannel().sendToServer(
					new CobblemonSeatPokemonPacket(seatPos, currentBoxIndex, dataIndex));
				onClose();
				return true;
			}
			slotY += SLOT_HEIGHT + SLOT_GAP;
			visibleCount++;
		}

		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (activeTab == Tab.PC && currentBoxData != null && !pcLoading) {
			List<Integer> presentIndices = new ArrayList<>();
			for (int i = 0; i < currentBoxData.size(); i++) {
				if (currentBoxData.get(i).present())
					presentIndices.add(i);
			}

			int maxScroll = Math.max(0, presentIndices.size() - MAX_VISIBLE_SLOTS);
			if (verticalAmount < 0) {
				pcScrollOffset = Math.min(pcScrollOffset + 1, maxScroll);
			} else if (verticalAmount > 0) {
				pcScrollOffset = Math.max(pcScrollOffset - 1, 0);
			}
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	// --- Utility ---

	private void renderBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
		graphics.fill(x, y, x + w, y + 1, color);
		graphics.fill(x, y + h - 1, x + w, y + h, color);
		graphics.fill(x, y, x + 1, y + h, color);
		graphics.fill(x + w - 1, y, x + w, y + h, color);
	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty())
			return str;
		return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
	}
}
