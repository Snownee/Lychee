package snownee.lychee.util;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.BlockSource;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.Mod;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KEvent;
import snownee.kiwi.util.Util;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.action.CustomAction;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.compat.fabric_recipe_api.AlwaysTrueIngredient;
import snownee.lychee.contextual.CustomCondition;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.context.LycheeContextSerializer;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.recipe.ILycheeRecipe;

@Mod(Lychee.ID)
public class CommonProxy implements ModInitializer {
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
			var itementity = new ItemEntity(
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
			return list.get(0);
		}
		var index = (System.currentTimeMillis() / interval) % list.size();
		return list.get(Math.toIntExact(index));
	}

	public static ResourceLocation readNullableRL(FriendlyByteBuf buf) {
		var string = buf.readUtf();
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

	public static <T> T readRegistryId(Registry<T> registry, FriendlyByteBuf buf) {
		return registry.byId(buf.readVarInt());
	}

	public static <T> void writeRegistryId(Registry<T> registry, T entry, FriendlyByteBuf buf) {
		buf.writeVarInt(registry.getId(entry));
	}

	@Nullable
	public static RecipeHolder<?> recipe(ResourceLocation id) {
		var manager = Util.getRecipeManager();
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

	public static <T> List<T> tagElements(Registry<T> registry, TagKey<T> tag) {
		return Streams.stream(registry.getTagOrEmpty(tag)).map(Holder::value).toList();
	}

	public static boolean isSimpleIngredient(Ingredient ingredient) {
		return !ingredient.requiresTesting();
	}

	public static void itemstackToJson(ItemStack stack, JsonObject jsonObject) {
		jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		if (!stack.getComponents().isEmpty()) {
			// TODO
			jsonObject.addProperty("nbt", stack.getComponents().toString());
		}
		if (stack.getCount() > 1) {
			jsonObject.addProperty("count", stack.getCount());
		}
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

	public static IngredientInfo.Type getIngredientType(Ingredient ingredient) {
		if (ingredient.isEmpty()) {
			return IngredientInfo.Type.AIR;
		}
		var customIngredient = ingredient.getCustomIngredient();
		if (customIngredient != null && customIngredient.getSerializer() == AlwaysTrueIngredient.SERIALIZER) {
			return IngredientInfo.Type.ANY;
		}
		return IngredientInfo.Type.NORMAL;
	}

	public static ItemStack dispensePlacement(BlockSource pSource, ItemStack pStack, Direction direction) {
		if (!(pStack.getItem() instanceof BlockItem item)) {
			return pStack;
		}
		var blockpos = pSource.pos().relative(direction);
		var state = pSource.level().getBlockState(blockpos);
		if (FallingBlock.isFree(state)) {
			item.place(new DirectionalPlaceContext(pSource.level(), blockpos, direction, pStack, direction));
		}
		return pStack;
	}

	@Override
	public void onInitialize() {
		RecipeTypes.init();
		Objects.requireNonNull(LycheeTags.FIRE_IMMUNE);
		Objects.requireNonNull(LycheeRegistries.CONTEXTUAL);
		Objects.requireNonNull(ContextualConditionType.AND);
		Objects.requireNonNull(PostActionTypes.DROP_ITEM);
		Objects.requireNonNull(RecipeSerializers.ITEM_BURNING);
		Objects.requireNonNull(LycheeContextKey.ACTION);
		Objects.requireNonNull(LycheeContextSerializer.ACTION);
		CustomIngredientSerializer.register(AlwaysTrueIngredient.SERIALIZER);

		// Interaction recipes
		UseBlockCallback.EVENT.register(BlockInteractingRecipe::invoke);
		AttackBlockCallback.EVENT.register(BlockClickingRecipe::invoke);

		// Dripstone recipes
		Registry.register(
				BuiltInRegistries.PARTICLE_TYPE,
				new ResourceLocation(Lychee.ID, "dripstone_dripping"),
				DripstoneParticleService.DRIPSTONE_DRIPPING
		);
		Registry.register(
				BuiltInRegistries.PARTICLE_TYPE,
				new ResourceLocation(Lychee.ID, "dripstone_falling"),
				DripstoneParticleService.DRIPSTONE_FALLING
		);
		Registry.register(
				BuiltInRegistries.PARTICLE_TYPE,
				new ResourceLocation(Lychee.ID, "dripstone_splash"),
				DripstoneParticleService.DRIPSTONE_SPLASH
		);
	}

	public interface CustomActionListener {
		boolean on(String id, CustomAction action, ILycheeRecipe<?> recipe);
	}

	public interface CustomConditionListener {
		boolean on(String id, CustomCondition condition);
	}
}
