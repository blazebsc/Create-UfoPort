package com.simibubi.create.compat.cobblemon;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class CobblemonPortraitRenderer {

	private static final Logger LOGGER = LogUtils.getLogger();

	private static boolean initialized = false;
	private static boolean available = false;

	private static Method drawPortraitMethod;
	private static Constructor<?> floatingStateConstructor;
	private static Method setAspectsMethod;
	private static Class<?> posableStateClass;

	private static Object speciesRegistryInstance;
	private static Method getByNameMethod;
	private static Method getStandardFormMethod;
	private static Method getBaseScaleMethod;
	private static Method getResourceIdentifierMethod;

	// Sprite rendering
	private static Object modelRepositoryInstance;
	private static Method getSpriteMethod;
	private static Object spriteTypePortrait;
	private static boolean spriteSystemAvailable = false;

	private static synchronized boolean ensureInitialized() {
		if (initialized)
			return available;
		initialized = true;

		try {
			Class<?> guiUtilsClass = Class.forName("com.cobblemon.mod.common.api.gui.GuiUtilsKt");
			posableStateClass = Class.forName(
					"com.cobblemon.mod.common.client.render.models.blockbench.PosableState");
			Class<?> floatingStateClass = Class.forName(
					"com.cobblemon.mod.common.client.render.models.blockbench.FloatingState");

			floatingStateConstructor = floatingStateClass.getConstructor();

			drawPortraitMethod = guiUtilsClass.getMethod("drawPosablePortrait",
					ResourceLocation.class, PoseStack.class,
					float.class, float.class, boolean.class,
					posableStateClass, float.class);

			setAspectsMethod = posableStateClass.getMethod("setCurrentAspects", Set.class);

			Class<?> pokemonSpeciesClass = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies");
			speciesRegistryInstance = pokemonSpeciesClass.getField("INSTANCE").get(null);
			getByNameMethod = speciesRegistryInstance.getClass().getMethod("getByName", String.class);

			// Try to initialize sprite system
			try {
				Class<?> spriteTypeClass = Class.forName("com.cobblemon.mod.common.client.render.models.blockbench.repository.SpriteType");
				Object[] spriteTypes = spriteTypeClass.getEnumConstants();
				if (spriteTypes != null) {
					for (Object st : spriteTypes) {
						if ("PORTRAIT".equals(st.toString())) {
							spriteTypePortrait = st;
							break;
						}
					}
				}

				Class<?> repoClass = Class.forName("com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository");
				modelRepositoryInstance = repoClass.getField("INSTANCE").get(null);
				getSpriteMethod = repoClass.getMethod("getSprite", ResourceLocation.class, posableStateClass, spriteTypeClass);

				if (spriteTypePortrait != null) {
					spriteSystemAvailable = true;
					LOGGER.info("Cobblemon sprite system initialized successfully");
				}
			} catch (Exception e) {
				LOGGER.info("Cobblemon sprite system not available, using 3D portraits: {}", e.getMessage());
			}

			LOGGER.info("Cobblemon portrait rendering initialized successfully");
			available = true;
		} catch (Exception e) {
			LOGGER.warn("Cobblemon portrait rendering not available: {}", e.getMessage());
			available = false;
		}

		return available;
	}

	public static Object createState(Set<String> aspects) {
		if (!ensureInitialized())
			return null;
		try {
			Object state = floatingStateConstructor.newInstance();
			if (aspects != null && !aspects.isEmpty()) {
				setAspectsMethod.invoke(state, aspects);
			}
			return state;
		} catch (Exception e) {
			LOGGER.warn("Failed to create FloatingState", e);
			return null;
		}
	}

	public static float getBaseScale(String speciesName) {
		if (!ensureInitialized())
			return 1.0f;
		try {
			String lookupName = speciesName.toLowerCase().replaceAll("[^a-z0-9]", "");
			Object species = getByNameMethod.invoke(speciesRegistryInstance, lookupName);
			if (species == null)
				return 1.0f;

			if (getStandardFormMethod == null)
				getStandardFormMethod = species.getClass().getMethod("getStandardForm");
			Object form = getStandardFormMethod.invoke(species);

			if (getBaseScaleMethod == null)
				getBaseScaleMethod = form.getClass().getMethod("getBaseScale");
			return (float) getBaseScaleMethod.invoke(form);
		} catch (Exception e) {
			LOGGER.warn("Failed to get base scale for {}", speciesName, e);
			return 1.0f;
		}
	}

	private static String toShowdownId(String speciesName) {
		return speciesName.toLowerCase().replaceAll("[^a-z0-9]", "");
	}

	private static ResourceLocation resolveSpeciesIdentifier(String speciesName) {
		try {
			String lookupName = toShowdownId(speciesName);
			Object species = getByNameMethod.invoke(speciesRegistryInstance, lookupName);
			if (species != null) {
				if (getResourceIdentifierMethod == null)
					getResourceIdentifierMethod = species.getClass().getMethod("getResourceIdentifier");
				Object identifier = getResourceIdentifierMethod.invoke(species);
				if (identifier instanceof ResourceLocation rl)
					return rl;
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to resolve species identifier for {}", speciesName, e);
		}
		return null;
	}

	/**
	 * Tries to render a 2D sprite texture for the Pokemon.
	 * Returns true if a sprite was rendered, false if caller should fall back to 3D portrait.
	 */
	public static boolean renderSprite(GuiGraphics graphics, String speciesName,
			Object state, int x, int y, int size) {
		if (!ensureInitialized() || !spriteSystemAvailable || state == null)
			return false;

		try {
			ResourceLocation identifier = resolveSpeciesIdentifier(speciesName);
			if (identifier == null)
				return false;

			Object spriteRL = getSpriteMethod.invoke(modelRepositoryInstance, identifier, state, spriteTypePortrait);
			if (spriteRL instanceof ResourceLocation spriteLoc) {
				RenderSystem.enableBlend();
				graphics.blit(spriteLoc, x, y, size, size, 0, 0, 128, 128, 128, 128);
				RenderSystem.disableBlend();
				return true;
			}
		} catch (Exception e) {
			// Sprite not available for this species, fall through
		}
		return false;
	}

	/**
	 * Renders a Pokemon portrait (3D model) at the given position.
	 */
	public static void renderPortrait(GuiGraphics graphics, String speciesName,
			Object state, int portraitX, int portraitY, int portraitSize,
			float contextScale, float partialTicks) {
		if (!ensureInitialized() || state == null)
			return;

		try {
			ResourceLocation identifier = resolveSpeciesIdentifier(speciesName);
			if (identifier == null)
				return;

			PoseStack poseStack = graphics.pose();

			poseStack.pushPose();

			poseStack.translate(
					portraitX + portraitSize / 2.0 - 1.0,
					portraitY - 12.0,
					0.0);

			drawPortraitMethod.invoke(null,
					identifier, poseStack, 13.0f, contextScale, false, state, partialTicks);

			poseStack.popPose();
		} catch (Exception e) {
			LOGGER.warn("Failed to render portrait for {}", speciesName, e);
		}
	}

	public static boolean isSpriteAvailable() {
		ensureInitialized();
		return spriteSystemAvailable;
	}

	public static boolean isAvailable() {
		return ensureInitialized();
	}
}
