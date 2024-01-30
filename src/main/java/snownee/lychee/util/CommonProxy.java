package snownee.lychee.util;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.Mod;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeConfig;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.action.CustomAction;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.compat.fabric_recipe_api.AlwaysTrueIngredient;
import snownee.lychee.contextual.CustomCondition;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.mixin.RecipeManagerAccess;
import snownee.lychee.recipes.dripstone_dripping.DripstoneRecipeMod;
import snownee.lychee.recipes.interaction.InteractionRecipeMod;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.recipe.LycheeRecipe;

@Mod(Lychee.ID)
public class CommonProxy implements ModInitializer {
	public static final Event<CustomActionListener> CUSTOM_ACTION_EVENT = EventFactory.createArrayBacked(
			CustomActionListener.class,
			listeners -> (id, action, recipe, patchContext) -> {
				for (CustomActionListener listener : listeners) {
					if (listener.on(id, action, recipe, patchContext)) {
						return true;
					}
				}
				return false;
			}
	);
	public static final Event<CustomConditionListener> CUSTOM_CONDITION_EVENT = EventFactory.createArrayBacked(
			CustomConditionListener.class,
			listeners -> (id, condition) -> {
				for (CustomConditionListener listener : listeners) {
					if (listener.on(id, condition)) {
						return true;
					}
				}
				return false;
			}
	);
	private static final Random RANDOM = new Random();
	public static boolean hasKiwi = isModLoaded("kiwi");
	public static boolean hasDFLib = isModLoaded("dripstone_fluid_lib");
	private static RecipeManager recipeManager;

	public static void dropItemStack(
			Level pLevel,
			double pX,
			double pY,
			double pZ,
			ItemStack pStack,
			@Nullable Consumer<ItemEntity> extraStep
	) {
		while (!pStack.isEmpty()) {
			ItemEntity itementity = new ItemEntity(
					pLevel,
					pX + RANDOM.nextGaussian() * 0.1 - 0.05,
					pY,
					pZ + RANDOM.nextGaussian() * 0.1 - 0.05,
					pStack.split(Math.min(RANDOM.nextInt(21) + 10, pStack.getMaxStackSize()))
			);
			itementity.setDeltaMovement(
					RANDOM.nextGaussian() * 0.05 - 0.025,
					RANDOM.nextGaussian() * 0.05 + 0.2,
					RANDOM.nextGaussian() * 0.05 - 0.025
			);
			if (extraStep != null) {
				extraStep.accept(itementity);
			}
			pLevel.addFreshEntity(itementity);
		}
	}

	public static String makeDescriptionId(String pType, @Nullable ResourceLocation pId) {
		return pId == null
			   ? pType + ".unregistered_sadface"
			   : pType + "." + wrapNamespace(pId.getNamespace()) + "." + pId.getPath().replace('/', '.');
	}

	public static String wrapNamespace(String modid) {
		return ResourceLocation.DEFAULT_NAMESPACE.equals(modid) ? Lychee.ID : modid;
	}

	public static MutableComponent white(CharSequence s) {
		return Component.literal(s.toString()).withStyle(ChatFormatting.WHITE);
	}

	public static String chance(float chance) {
		return (chance < 0.01 ? "<1" : String.valueOf((int) (chance * 100))) + "%";
	}

	public static String capitaliseAllWords(String str) {
		int sz = str.length();
		StringBuilder buffer = new StringBuilder(sz);
		boolean space = true;
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch)) {
				buffer.append(ch);
				space = true;
			} else if (space) {
				buffer.append(Character.toTitleCase(ch));
				space = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	public static <T> T getCycledItem(List<T> list, T fallback, int interval) {
		if (list.isEmpty()) {
			return fallback;
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		long index = (System.currentTimeMillis() / interval) % list.size();
		return list.get(Math.toIntExact(index));
	}

	public static ResourceLocation readNullableRL(FriendlyByteBuf buf) {
		String string = buf.readUtf();
		if (string.isEmpty()) {
			return null;
		} else {
			return new ResourceLocation(string);
		}
	}

	public static void writeNullableRL(ResourceLocation rl, FriendlyByteBuf buf) {
		if (rl == null) {
			buf.writeUtf("");
		} else {
			buf.writeUtf(rl.toString());
		}
	}

	public static InteractionResult interactionResult(Boolean bool) {
		if (bool == null) {
			return InteractionResult.PASS;
		}
		return bool ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}

	public static <T> T readRegistryId(Registry<T> registry, FriendlyByteBuf buf) {
		return registry.byId(buf.readVarInt());
	}

	public static <T> void writeRegistryId(Registry<T> registry, T entry, FriendlyByteBuf buf) {
		buf.writeVarInt(registry.getId(entry));
	}

	public static RecipeManager recipeManager() {
		if (recipeManager == null && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			if (LycheeConfig.debug) {
				Lychee.LOGGER.trace("Early loading recipes..");
			}
			recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
		}
		return recipeManager;
	}

	public static void setRecipeManager(RecipeManager recipeManager) {
		CommonProxy.recipeManager = recipeManager;
		if (LycheeConfig.debug) {
			Lychee.LOGGER.trace("Setting recipe manager..");
		}
	}

	@Nullable
	public static RecipeHolder<?> recipe(ResourceLocation id) {
		return recipeManager().byKey(id).orElse(null);
	}

	public static <C extends Container, T extends Recipe<C>> Collection<RecipeHolder<T>> recipes(RecipeType<T> type) {
		return ((RecipeManagerAccess) recipeManager()).callByType(type).values();
	}

	// see Entity.getOnPos
	public static BlockPos getOnPos(Entity entity) {
		int i = Mth.floor(entity.getX());
		int j = Mth.floor(entity.getY() - 0.05); // vanilla is 0.2. carpet's height is 0.13
		int k = Mth.floor(entity.getZ());
		BlockPos blockpos = new BlockPos(i, j, k);
		if (entity.level().isEmptyBlock(blockpos)) {
			BlockPos blockpos1 = blockpos.below();
			BlockState blockstate = entity.level().getBlockState(blockpos1);
			if (collisionExtendsVertically(blockstate, entity.level(), blockpos1, entity)) {
				return blockpos1;
			}
		}
		return blockpos;
	}

	public static boolean collisionExtendsVertically(BlockState state, Level level, BlockPos pos, Entity entity) {
		return state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.getBlock() instanceof FenceGateBlock;
	}

	public static Vec3 clampPos(Vec3 origin, BlockPos pos) {
		double x = clamp(origin.x, pos.getX());
		double y = clamp(origin.y, pos.getY());
		double z = clamp(origin.z, pos.getZ());
		if (x == origin.x && y == origin.y && z == origin.z) {
			return origin;
		}
		return new Vec3(x, y, z);
	}

	private static double clamp(double v, int target) {
		if (v < target) {
			return target;
		}
		if (v >= target + 1) {
			return target + 0.999999;
		}
		return v;
	}

	public static <T> List<T> tagElements(Registry<T> registry, TagKey<T> tag) {
		return Streams.stream(registry.getTagOrEmpty(tag)).map(Holder::value).toList();
	}

	public static boolean isSimpleIngredient(Ingredient ingredient) {
		return !ingredient.requiresTesting();
	}

	public static BlockPos parseOffset(JsonObject o) {
		int x = GsonHelper.getAsInt(o, "offsetX", 0);
		int y = GsonHelper.getAsInt(o, "offsetY", 0);
		int z = GsonHelper.getAsInt(o, "offsetZ", 0);
		BlockPos offset = BlockPos.ZERO;
		if (x != 0 || y != 0 || z != 0) {
			offset = new BlockPos(x, y, z);
		}
		return offset;
	}

	public static boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static void itemstackToJson(ItemStack stack, JsonObject jsonObject) {
		jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		if (stack.hasTag()) {
			jsonObject.addProperty("nbt", stack.getTag().toString());
		}
		if (stack.getCount() > 1) {
			jsonObject.addProperty("count", stack.getCount());
		}
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static JsonObject tagToJson(CompoundTag tag) {
		return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag).getAsJsonObject();
	}

	public static CompoundTag jsonToTag(JsonElement json) {
		if (json.isJsonObject()) {
			return (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
		} else {
			try {
				return TagParser.parseTag(json.getAsString());
			} catch (CommandSyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	public static void registerCustomActionListener(CustomActionListener listener) {
		CUSTOM_ACTION_EVENT.register(listener);
	}

	public static void registerCustomConditionListener(CustomConditionListener listener) {
		CUSTOM_CONDITION_EVENT.register(listener);
	}

	public static void postCustomActionEvent(
			String id,
			CustomAction action,
			LycheeRecipe<?> recipe,
			LycheeRecipe.NBTPatchContext patchContext
	) {
		CUSTOM_ACTION_EVENT.invoker().on(id, action, recipe, patchContext);
	}

	public static void postCustomConditionEvent(String id, CustomCondition condition) {
		CUSTOM_CONDITION_EVENT.invoker().on(id, condition);
	}

	public static IngredientInfo.Type getIngredientType(Ingredient ingredient) {
		if (ingredient == OldLycheeRecipe.Serializer.EMPTY_INGREDIENT) {
			return IngredientInfo.Type.AIR;
		}
		CustomIngredient customIngredient = ingredient.getCustomIngredient();
		if (customIngredient != null && customIngredient.getSerializer() == AlwaysTrueIngredient.SERIALIZER) {
			return IngredientInfo.Type.ANY;
		}
		return IngredientInfo.Type.NORMAL;
	}

	public static boolean hasModdedDripParticle(FluidState fluid) {
		if (hasDFLib && fluid.getType() instanceof DripstoneInteractingFluid) {
			return true;
		}
		return false;
	}

	public static ParticleType<BlockParticleOption> registerParticleType(ParticleOptions.Deserializer<BlockParticleOption> deserializer) {
		return FabricParticleTypes.complex(deserializer);
	}

	public static ItemStack dispensePlacement(BlockSource pSource, ItemStack pStack, Direction direction) {
		if (!(pStack.getItem() instanceof BlockItem item)) {
			return pStack;
		}
		BlockPos blockpos = pSource.getPos().relative(direction);
		BlockState state = pSource.getLevel().getBlockState(blockpos);
		if (FallingBlock.isFree(state)) {
			item.place(new DirectionalPlaceContext(pSource.getLevel(), blockpos, direction, pStack, direction));
		}
		return pStack;
	}

	@Override
	public void onInitialize() {
		if (hasKiwi) {
			FabricLoader.getInstance()
						.getModContainer("kiwi")
						.map(ModContainer::getMetadata)
						.map(ModMetadata::getVersion)
						.ifPresent(version -> {
							try {
								Version minVersion = Version.parse("11.1.1");
								if (minVersion.compareTo(version) > 0) {
									throw new RuntimeException(
											"Kiwi version is too low! Please update to at least 11.1.1. You have %s".formatted(
													version));
								}
							} catch (VersionParsingException e) {
								throw new RuntimeException(e);
							}
						});
		}

		RecipeTypes.init();
		LycheeTags.init();
		LycheeRegistries.init();
		ContextualConditionTypes.init();
		PostActionTypes.init();
		RecipeSerializers.init();
		CustomIngredientSerializer.register(AlwaysTrueIngredient.SERIALIZER);

		// Interaction recipes
		UseBlockCallback.EVENT.register(InteractionRecipeMod::useItemOn);
		AttackBlockCallback.EVENT.register(InteractionRecipeMod::clickItemOn);

		// Dripstone recipes
		Registry.register(
				BuiltInRegistries.PARTICLE_TYPE,
				new ResourceLocation(Lychee.ID, "dripstone_dripping"),
				DripstoneRecipeMod.DRIPSTONE_DRIPPING
		);
		Registry.register(
				BuiltInRegistries.PARTICLE_TYPE,
				new ResourceLocation(Lychee.ID, "dripstone_falling"),
				DripstoneRecipeMod.DRIPSTONE_FALLING
		);
		Registry.register(
				BuiltInRegistries.PARTICLE_TYPE,
				new ResourceLocation(Lychee.ID, "dripstone_splash"),
				DripstoneRecipeMod.DRIPSTONE_SPLASH
		);
	}

	public interface CustomActionListener {
		boolean on(
				String id, CustomAction action, LycheeRecipe<?> recipe,
				LycheeRecipe.NBTPatchContext patchContext
		);
	}

	public interface CustomConditionListener {
		boolean on(String id, CustomCondition condition);
	}
}
