package com.simibubi.create.compat.cobblemon;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CobblemonCompat {

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Set<String> DEPOSIT_ONLY_BLOCKS = Set.of(
		"cobblemon:fossil_analyzer",
		"cobblemon:restoration_tank"
	);

	private static final Set<String> INTERACT_BLOCKS = Set.of(
		"cobblemon:display_case",
		"cobblemon:gilded_chest",
		"cobblemon:blue_gilded_chest",
		"cobblemon:yellow_gilded_chest",
		"cobblemon:pink_gilded_chest",
		"cobblemon:black_gilded_chest",
		"cobblemon:white_gilded_chest",
		"cobblemon:green_gilded_chest",
		"cobblemon:gimmighoul_chest"
	);

	public static void init() {
		registerArmInteractionTypes();
	}

	private static void registerArmInteractionTypes() {
		ResourceLocation depositId = ResourceLocation.fromNamespaceAndPath("create", "cobblemon_deposit");
		ArmInteractionPointType.register(new CobblemonDepositType(depositId));

		ResourceLocation interactId = ResourceLocation.fromNamespaceAndPath("create", "cobblemon_interact");
		ArmInteractionPointType.register(new CobblemonInteractType(interactId));
	}

	private static String getBlockId(BlockState state) {
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
	}

	// --- Pokemon Entity Check ---

	private static final String SEAT_SPAWNED_TAG = "CreateSeatSpawned";

	public static boolean isPokemonEntity(Entity entity) {
		try {
			Class<?> pokemonEntityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
			return pokemonEntityClass.isInstance(entity);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean isSeatSpawnedPokemon(Entity entity) {
		return isPokemonEntity(entity) && entity.getTags().contains(SEAT_SPAWNED_TAG);
	}

	public static BlockPos findAdjacentPosition(Level world, BlockPos seatPos) {
		// Try cardinal directions first
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos adjacent = seatPos.relative(dir);
			if (world.getBlockState(adjacent).isAir() || !world.getBlockState(adjacent).isSolid()) {
				return adjacent;
			}
		}
		// Fallback: above the seat
		return seatPos.above();
	}

	// --- Blaze Burner Pokemon Capture ---

	private static final Set<String> BURNER_CAPTURABLE_SPECIES = Set.of(
		"magmar"
	);

	public static boolean isBurnerCapturable(Entity entity) {
		try {
			Class<?> pokemonEntityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
			if (!pokemonEntityClass.isInstance(entity))
				return false;

			Object pokemon = pokemonEntityClass.getMethod("getPokemon").invoke(entity);
			Object species = pokemon.getClass().getMethod("getSpecies").invoke(pokemon);
			Object speciesName = species.getClass().getMethod("getName").invoke(species);

			return BURNER_CAPTURABLE_SPECIES.contains(speciesName.toString().toLowerCase());
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isOwnedPokemon(Entity entity) {
		try {
			Class<?> pokemonEntityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
			if (!pokemonEntityClass.isInstance(entity))
				return false;

			Object pokemon = pokemonEntityClass.getMethod("getPokemon").invoke(entity);
			Object ownerUUID = pokemon.getClass().getMethod("getOwnerUUID").invoke(pokemon);
			return ownerUUID != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static UUID getPokemonOwnerUUID(Entity entity) {
		try {
			Class<?> pokemonEntityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
			if (!pokemonEntityClass.isInstance(entity))
				return null;

			Object pokemon = pokemonEntityClass.getMethod("getPokemon").invoke(entity);
			Object ownerUUID = pokemon.getClass().getMethod("getOwnerUUID").invoke(pokemon);
			return (UUID) ownerUUID;
		} catch (Exception e) {
			return null;
		}
	}

	// --- Seat Pokemon Selection ---

	public record PartySlotData(boolean present, String speciesName, int level, List<String> aspects) {}

	public static List<PartySlotData> getPartyData(ServerPlayer player) {
		List<PartySlotData> result = new ArrayList<>();
		try {
			Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
			Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemonClass.getField("INSTANCE").get(null));
			Object party = storage.getClass().getMethod("getParty", ServerPlayer.class).invoke(storage, player);

			for (int i = 0; i < 6; i++) {
				Object pokemon = party.getClass().getMethod("get", int.class).invoke(party, i);
				if (pokemon != null) {
					Object species = pokemon.getClass().getMethod("getSpecies").invoke(pokemon);
					String speciesName = species.getClass().getMethod("getName").invoke(species).toString();
					int level = (int) pokemon.getClass().getMethod("getLevel").invoke(pokemon);
					@SuppressWarnings("unchecked")
					Set<String> aspectsSet = (Set<String>) pokemon.getClass().getMethod("getAspects").invoke(pokemon);
					List<String> aspects = new ArrayList<>(aspectsSet);
					result.add(new PartySlotData(true, speciesName, level, aspects));
				} else {
					result.add(new PartySlotData(false, "", 0, List.of()));
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to read Cobblemon party data", e);
			for (int i = result.size(); i < 6; i++)
				result.add(new PartySlotData(false, "", 0, List.of()));
		}
		return result;
	}

	public static boolean hasPartyPokemon(ServerPlayer player) {
		try {
			Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
			Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemonClass.getField("INSTANCE").get(null));
			Object party = storage.getClass().getMethod("getParty", ServerPlayer.class).invoke(storage, player);

			for (int i = 0; i < 6; i++) {
				Object pokemon = party.getClass().getMethod("get", int.class).invoke(party, i);
				if (pokemon != null)
					return true;
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to check Cobblemon party", e);
		}
		return false;
	}

	public static int getPCBoxCount(ServerPlayer player) {
		try {
			Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
			Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemonClass.getField("INSTANCE").get(null));
			Object pc = storage.getClass().getMethod("getPC", ServerPlayer.class).invoke(storage, player);
			List<?> boxes = (List<?>) pc.getClass().getMethod("getBoxes").invoke(pc);
			return boxes.size();
		} catch (Exception e) {
			LOGGER.warn("Failed to get PC box count", e);
			return 0;
		}
	}

	public static List<PartySlotData> getPCBoxData(ServerPlayer player, int boxIndex) {
		List<PartySlotData> result = new ArrayList<>();
		try {
			Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
			Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemonClass.getField("INSTANCE").get(null));
			Object pc = storage.getClass().getMethod("getPC", ServerPlayer.class).invoke(storage, player);
			List<?> boxes = (List<?>) pc.getClass().getMethod("getBoxes").invoke(pc);
			if (boxIndex < 0 || boxIndex >= boxes.size()) return result;
			Object box = boxes.get(boxIndex);
			for (int i = 0; i < 30; i++) {
				Object pokemon = box.getClass().getMethod("get", int.class).invoke(box, i);
				if (pokemon != null) {
					Object species = pokemon.getClass().getMethod("getSpecies").invoke(pokemon);
					String speciesName = species.getClass().getMethod("getName").invoke(species).toString();
					int level = (int) pokemon.getClass().getMethod("getLevel").invoke(pokemon);
					@SuppressWarnings("unchecked")
					Set<String> aspectsSet = (Set<String>) pokemon.getClass().getMethod("getAspects").invoke(pokemon);
					List<String> aspects = new ArrayList<>(aspectsSet);
					result.add(new PartySlotData(true, speciesName, level, aspects));
				} else {
					result.add(new PartySlotData(false, "", 0, List.of()));
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to read PC box data", e);
		}
		return result;
	}

	public static String getPCBoxName(ServerPlayer player, int boxIndex) {
		try {
			Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
			Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemonClass.getField("INSTANCE").get(null));
			Object pc = storage.getClass().getMethod("getPC", ServerPlayer.class).invoke(storage, player);
			List<?> boxes = (List<?>) pc.getClass().getMethod("getBoxes").invoke(pc);
			if (boxIndex < 0 || boxIndex >= boxes.size()) return "Box " + (boxIndex + 1);
			Object box = boxes.get(boxIndex);
			Object name = box.getClass().getMethod("getName").invoke(box);
			return name != null ? name.toString() : "Box " + (boxIndex + 1);
		} catch (Exception e) {
			return "Box " + (boxIndex + 1);
		}
	}

	public static void spawnAndSeatPokemonFromPC(ServerPlayer player, BlockPos seatPos, int boxIndex, int slotIndex) {
		try {
			Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
			Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemonClass.getField("INSTANCE").get(null));
			Object pc = storage.getClass().getMethod("getPC", ServerPlayer.class).invoke(storage, player);
			List<?> boxes = (List<?>) pc.getClass().getMethod("getBoxes").invoke(pc);
			if (boxIndex < 0 || boxIndex >= boxes.size()) return;
			Object box = boxes.get(boxIndex);
			Object pokemon = box.getClass().getMethod("get", int.class).invoke(box, slotIndex);
			if (pokemon == null) return;

			// Check if this Pokemon is already sent out in the world
			Object existingEntity = pokemon.getClass().getMethod("getEntity").invoke(pokemon);
			if (existingEntity != null) return;

			Class<?> pokemonEntityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
			Class<?> pokemonClass = Class.forName("com.cobblemon.mod.common.pokemon.Pokemon");
			Class<?> cobblemonEntities = Class.forName("com.cobblemon.mod.common.CobblemonEntities");
			Object entitiesInstance = cobblemonEntities.getField("INSTANCE").get(null);
			Object pokemonEntityType = cobblemonEntities.getField("POKEMON").get(entitiesInstance);

			Constructor<?> constructor = pokemonEntityClass.getConstructor(
				Level.class, pokemonClass, EntityType.class);
			Entity pokemonEntity = (Entity) constructor.newInstance(
				player.level(), pokemon, pokemonEntityType);

			pokemonEntity.setPos(seatPos.getX() + 0.5, seatPos.getY(), seatPos.getZ() + 0.5);
			pokemonEntity.addTag(SEAT_SPAWNED_TAG);
			player.level().addFreshEntity(pokemonEntity);
			SeatBlock.sitDown(player.level(), seatPos, pokemonEntity);
		} catch (Exception e) {
			LOGGER.warn("Failed to spawn and seat PC Pokemon", e);
		}
	}

	public static void spawnAndSeatPokemon(ServerPlayer player, BlockPos seatPos, int partySlot) {
		try {
			Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
			Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemonClass.getField("INSTANCE").get(null));
			Object party = storage.getClass().getMethod("getParty", ServerPlayer.class).invoke(storage, player);
			Object pokemon = party.getClass().getMethod("get", int.class).invoke(party, partySlot);

			if (pokemon == null) {
				LOGGER.warn("No Pokemon in party slot {}", partySlot);
				return;
			}

			// Check if this Pokemon is already sent out in the world
			Object existingEntity = pokemon.getClass().getMethod("getEntity").invoke(pokemon);
			if (existingEntity != null) return;

			// Kotlin PokemonEntity has a 3-arg constructor: (Level, Pokemon, EntityType)
			// CobblemonEntities.POKEMON is @JvmField so it's a field, not a getter
			Class<?> pokemonEntityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
			Class<?> pokemonClass = Class.forName("com.cobblemon.mod.common.pokemon.Pokemon");
			Class<?> cobblemonEntities = Class.forName("com.cobblemon.mod.common.CobblemonEntities");
			Object entitiesInstance = cobblemonEntities.getField("INSTANCE").get(null);
			Object pokemonEntityType = cobblemonEntities.getField("POKEMON").get(entitiesInstance);

			Constructor<?> constructor = pokemonEntityClass.getConstructor(
				Level.class, pokemonClass, EntityType.class);
			Entity pokemonEntity = (Entity) constructor.newInstance(
				player.level(), pokemon, pokemonEntityType);

			pokemonEntity.setPos(seatPos.getX() + 0.5, seatPos.getY(), seatPos.getZ() + 0.5);
			pokemonEntity.addTag(SEAT_SPAWNED_TAG);
			player.level().addFreshEntity(pokemonEntity);
			SeatBlock.sitDown(player.level(), seatPos, pokemonEntity);
		} catch (Exception e) {
			LOGGER.warn("Failed to spawn and seat Pokemon", e);
		}
	}

	public static class CobblemonDepositType extends ArmInteractionPointType {
		public CobblemonDepositType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return DEPOSIT_ONLY_BLOCKS.contains(getBlockId(state));
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class CobblemonInteractType extends ArmInteractionPointType {
		public CobblemonInteractType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return INTERACT_BLOCKS.contains(getBlockId(state));
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}
}
