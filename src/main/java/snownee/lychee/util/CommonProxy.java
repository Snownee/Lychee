package snownee.lychee.util;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.recipe.CustomIngredientSerializer;
import snownee.kiwi.util.KEvent;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.action.CustomAction;
import snownee.lychee.compat.recipe_api.AlwaysTrueIngredient;
import snownee.lychee.compat.recipe_api.VisualOnlyComponentsIngredient;
import snownee.lychee.compat.recipeviewer.IngredientType;
import snownee.lychee.contextual.CustomCondition;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.context.LycheeContextSerializers;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.UIElementType;

@Mod(Lychee.ID)
public class CommonProxy {
	public static final KEvent<CustomActionListener> CUSTOM_ACTION_EVENT = KEvent.createArrayBacked(
			CustomActionListener.class,
			listeners -> (id, action, recipe) -> {
				for (var listener : listeners) {
					if (listener.on(id, action, recipe)) {
						return true;
					}
				}
				return false;
			}
	);
	public static final KEvent<CustomConditionListener> CUSTOM_CONDITION_EVENT = KEvent.createArrayBacked(
			CustomConditionListener.class,
			listeners -> (id, condition) -> {
				for (var listener : listeners) {
					if (listener.on(id, condition)) {
						return true;
					}
				}
				return false;
			}
	);
	private static final Random RANDOM = new Random();
	public static boolean hasDFLib = Platform.isModLoaded("dripstone_fluid_lib");

	public static void dropItemStack(
			Level pLevel,
			double pX,
			double pY,
			double pZ,
			ItemStack pStack,
			@Nullable Consumer<ItemEntity> extraStep
	) {
		while (!pStack.isEmpty()) {
			var itementity = new ItemEntity(pLevel, pX, pY, pZ, pStack.split(Math.min(RANDOM.nextInt(21) + 10, pStack.getMaxStackSize())));
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
		if (chance >= 0.1F) {
			return (int) (chance * 100) + "%";
		} else if (chance >= 0.001F) {
			return String.format("%.1f%%", chance * 100);
		} else {
			return "<0.1%";
		}
	}

	public static String capitaliseAllWords(String str) {
		var sz = str.length();
		var buffer = new StringBuilder(sz);
		var space = true;
		for (var i = 0; i < sz; i++) {
			var ch = str.charAt(i);
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
			return list.getFirst();
		}
		var index = (System.currentTimeMillis() / interval) % list.size();
		return list.get(Math.toIntExact(index));
	}

	@Nullable
	public static RecipeHolder<?> recipe(ResourceLocation id) {
		var manager = KUtil.getRecipeManager();
		if (manager == null) {
			return null;
		}
		return manager.byKey(id).orElse(null);
	}

	// see Entity.getOnPos
	public static BlockPos getOnPos(Entity entity) {
		var i = Mth.floor(entity.getX());
		var j = Mth.floor(entity.getY() - 0.05); // vanilla is 0.2. carpet's height is 0.13
		var k = Mth.floor(entity.getZ());
		var blockpos = new BlockPos(i, j, k);
		if (entity.level().isEmptyBlock(blockpos)) {
			var blockpos1 = blockpos.below();
			var blockstate = entity.level().getBlockState(blockpos1);
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
		var x = clamp(origin.x, pos.getX());
		var y = clamp(origin.y, pos.getY());
		var z = clamp(origin.z, pos.getZ());
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

	public static boolean isSimpleIngredient(Ingredient ingredient) {
		return ingredient.isSimple();
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
			ILycheeRecipe<?> recipe
	) {
		CUSTOM_ACTION_EVENT.invoker().on(id, action, recipe);
	}

	public static void postCustomConditionEvent(String id, CustomCondition condition) {
		CUSTOM_CONDITION_EVENT.invoker().on(id, condition);
	}

	public static IngredientType getIngredientType(Ingredient ingredient) {
		var customIngredient = ingredient.getCustomIngredient();
		if (customIngredient != null && Objects.equals(
				NeoForgeRegistries.INGREDIENT_TYPES.getKey(customIngredient.getType()),
				AlwaysTrueIngredient.ID)) {
			return IngredientType.ANY;
		}
		if (ingredient.isEmpty()) { // TODO not compatible with AIR_INGREDIENT!
			return IngredientType.AIR;
		}
		return IngredientType.NORMAL;
	}

	public static boolean dispensePlacement(BlockSource pSource, ItemStack pStack, Direction direction) {
		if (!(pStack.getItem() instanceof BlockItem item)) {
			return false;
		}
		var blockpos = pSource.pos().relative(direction);
		var state = pSource.level().getBlockState(blockpos);
		if (!FallingBlock.isFree(state)) {
			return false;
		}
		if (item.getBlock() instanceof PointedDripstoneBlock block) {
			var blockState = block.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN);
			var entity = FallingBlockEntity.fall(pSource.level(), blockpos, blockState);
			var f = 6f;
			entity.setHurtsEntities(f, 40);
			pStack.shrink(1);
			return true;
		}
		try {
			item.place(new DirectionalPlaceContext(pSource.level(), blockpos, direction, pStack, direction));
		} catch (Exception exception) {
			Lychee.LOGGER.error("Error trying to place block at {}", blockpos, exception);
		}
		return false;
	}

	public static <T> String getTagTranslationKey(TagKey<T> key) {
		return Tags.getTagTranslationKey(key);
	}

	public static void hurtAndBreak(ItemStack itemStack, int damage, ServerLevel level, @Nullable LivingEntity entity) {
		itemStack.hurtAndBreak(damage, level, entity, $ -> {});
	}

	public CommonProxy(IEventBus modEventBus) {
		CustomIngredientSerializer.register(AlwaysTrueIngredient.SERIALIZER);
		CustomIngredientSerializer.register(VisualOnlyComponentsIngredient.SERIALIZER);
		modEventBus.addListener(LycheeRegistries::init);
		modEventBus.addListener(CommonProxy::register);

		// Interaction recipes
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			InteractionResult result = BlockInteractingRecipe.invoke(
					event.getEntity(),
					event.getLevel(),
					event.getHand(),
					event.getHitVec());
			event.setCanceled(result.consumesAction());
			event.setCancellationResult(result);
		});
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
			if (event.getFace() == null) {
				return;
			}
			InteractionResult result = BlockClickingRecipe.invoke(
					event.getEntity(),
					event.getLevel(),
					event.getHand(),
					event.getPos(),
					event.getFace());
			event.setCanceled(result != InteractionResult.PASS);
		});
		NeoForge.EVENT_BUS.addListener((BlockEvent.BreakEvent event) -> {
			if (BlockClickingRecipe.isBreakProtected(event.getPlayer(), (Level) event.getLevel(), event.getPos())) {
				event.setCanceled(true);
			}
		});
	}

	private static void register(RegisterEvent event) {
		event.register(LycheeRegistries.CONTEXT.key(), helper -> Objects.requireNonNull(LycheeContextKey.ACTION));
		event.register(LycheeRegistries.CONTEXT_SERIALIZER.key(), helper -> LycheeContextSerializers.init());
		event.register(LycheeRegistries.CONTEXTUAL.key(), helper -> Objects.requireNonNull(ContextualConditionType.AND));
		event.register(LycheeRegistries.POST_ACTION.key(), helper -> Objects.requireNonNull(PostActionTypes.DROP_ITEM));
		event.register(BuiltInRegistries.RECIPE_SERIALIZER.key(), helper -> Objects.requireNonNull(RecipeSerializers.ITEM_BURNING));
		event.register(BuiltInRegistries.RECIPE_TYPE.key(), helper -> Objects.requireNonNull(RecipeTypes.ALL));
		event.register(
				BuiltInRegistries.PARTICLE_TYPE.key(), helper -> {
					Registry.register(
							BuiltInRegistries.PARTICLE_TYPE,
							Lychee.id("dripstone_dripping"),
							DripstoneParticleService.DRIPSTONE_DRIPPING
					);
					Registry.register(
							BuiltInRegistries.PARTICLE_TYPE,
							Lychee.id("dripstone_falling"),
							DripstoneParticleService.DRIPSTONE_FALLING
					);
					Registry.register(
							BuiltInRegistries.PARTICLE_TYPE,
							Lychee.id("dripstone_splash"),
							DripstoneParticleService.DRIPSTONE_SPLASH
					);
				});
		event.register(LycheeRegistries.UI_ELEMENT.key(), helper -> Objects.requireNonNull(UIElementType.BLOCK));
	}

	public interface CustomActionListener {
		boolean on(String id, CustomAction action, ILycheeRecipe<?> recipe);
	}

	public interface CustomConditionListener {
		boolean on(String id, CustomCondition condition);
	}
}
