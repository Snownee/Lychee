package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.CraftingContainerLocation;
import snownee.lychee.context.CraftingContext;
import snownee.lychee.mixin.recipes.crafting.ShapedRecipeAccess;
import snownee.lychee.mixin.recipes.crafting.ShapedRecipePatternAccess;
import snownee.lychee.util.LycheeCraftingInput;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;


public class ShapedCraftingRecipe implements ILycheeRecipe<CraftingInput>, CraftingRecipe {
	public static final Cache<InputKey, ResolvableContext> CONTEXT_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.SECONDS)
			.build();

	public record InputKey(CraftingInput input) {
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (hashCode() == 0) {
				return false;
			}
			return o instanceof InputKey(CraftingInput input1) && hashCode() == ((LycheeCraftingInput) input1).lychee$hash() &&
					input.equals(input1);
		}

		@Override
		public int hashCode() {
			return ((LycheeCraftingInput) input).lychee$hash();
		}
	}

	public static class ResolvableContext {
		public @Nullable LycheeContext context;

		public ResolvableContext(@Nullable LycheeContext context) {
			this.context = context;
		}

		public @Nullable LycheeContext resolve(@Nullable Level newLevel) {
			if (context != null) {
				if (newLevel != null) {
					context.put(LycheeContextKey.LEVEL, newLevel);
				}
				return context;
			}
			if (newLevel == null) {
				return null;
			}
			context = new LycheeContext();
			context.put(LycheeContextKey.LEVEL, newLevel);
			return context;
		}
	}

	protected final LycheeRecipeCommonProperties commonProperties;
	protected final ShapedRecipe shaped;
	protected final List<PostAction> assemblingActions;

	public ShapedCraftingRecipe(LycheeRecipeCommonProperties commonProperties, ShapedRecipe shaped, List<PostAction> assemblingActions) {
		this.commonProperties = commonProperties;
		this.assemblingActions = assemblingActions;
		this.shaped = shaped;
		onConstructed();
	}

	public ShapedCraftingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			String group,
			CraftingBookCategory category,
			ShapedRecipePattern pattern,
			ItemStackTemplate result,
			boolean showNotification,
			List<PostAction> assemblingActions) {
		this(
				commonProperties,
				new ShapedRecipe(new CommonInfo(showNotification), new CraftingBookInfo(category, group), pattern, result),
				assemblingActions);
	}

	@SuppressWarnings("UnusedReturnValue")
	@Nullable
	public static ResolvableContext injectContext(@Nullable Container container, CraftingInput input) {
		if (!RecipeTypes.HAS_CRAFTING_RECIPES) {
			return null;
		}
		LycheeCraftingInput lycheeInput = (LycheeCraftingInput) input;
		if (lycheeInput.lychee$hash() == 0) {
			lycheeInput.lychee$setHash(input.hashCode() * 31 + (container == null ? 0 : System.identityHashCode(container)));
		}
		InputKey key = new InputKey(input);
		if (CONTEXT_CACHE.getIfPresent(key) != null) {
			return null;
		}
		LycheeContext context = null;
		CraftingContainerLocation location = null;
		try {
			if (container != null) {
				location = CraftingContext.CONTAINER_WORLD_LOCATOR.get(container.getClass()).apply(container);
			}
		} catch (ExecutionException ignored) {
		}

		if (location != null) {
			context = new LycheeContext();
			context.put(LycheeContextKey.LEVEL, location.level());
			final var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
			if (location.position() != null) {
				lootParams.set(LootContextParams.ORIGIN, location.position());
			}
			if (location.player() != null) {
				lootParams.set(LootContextParams.THIS_ENTITY, location.player());
			}
			if (container instanceof BlockEntity blockEntity) {
				lootParams.set(LootContextParams.BLOCK_ENTITY, blockEntity);
				lootParams.set(LootContextParams.BLOCK_STATE, blockEntity.getBlockState());
			}
		}
		ResolvableContext resolvableContext = new ResolvableContext(context);
		CONTEXT_CACHE.put(key, resolvableContext);
//		Lychee.LOGGER.info("Injected context: {}", key.hashCode());
		return resolvableContext;
	}

	@Override
	public LycheeRecipeCommonProperties commonProperties() {
		return commonProperties;
	}

	@Override
	public void onConstructed() {
		ILycheeRecipe.super.onConstructed();
	}

	@Override
	public IntList getItemIndexes(Reference reference) {
		return ILycheeRecipe.super.getItemIndexes(reference);
	}

	@Override
	public List<Ingredient> getIngredients() {
		return shaped.getIngredients().stream().filter(Optional::isPresent).map(Optional::get).toList();
	}

	@Override
	public int ingredientCount() {
		return getIngredients().size();
	}

	@Override
	public IntList getItemIndexes(JsonPointer pointer) {
		List<Ingredient> ingredients = getIngredients();
		var size = ingredients.size();
		if (pointer.size() == 1 && pointer.getString(0).equals("result")) {
			return IntList.of(size);
		}
		if (pointer.size() == 2 && pointer.getString(0).equals("key")) {
			var key = pointer.getString(1);
			if (key.length() != 1) {
				return IntList.of();
			}
			var pattern = ((ShapedRecipeAccess) shaped).getPattern();
			var dataOptional = ((ShapedRecipePatternAccess) (Object) pattern).data();
			if (dataOptional.isEmpty()) {
				return IntList.of();
			}
			var data = dataOptional.get();
			IntList list = IntArrayList.of();
			char cp = key.charAt(0);
			var ingredient = data.key().get(cp);
			for (var i = 0; i < ingredients.size(); i++) {
				if (ingredient == ingredients.get(i)) {
					list.add(i);
				}
			}
			return list;
		}
		return IntList.of(size);
	}

	@SuppressWarnings("UnreachableCode")
	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (ghost() || !shaped.matches(input, level)) {
			return false;
		}
		return updateContextAndGet(input, level) != null;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		var context = updateContextAndGet(input, null);
		if (context == null) {
			Lychee.LOGGER.warn("Failed to get context for recipe {} when assembling", input.items());
			return ItemStack.EMPTY;
		}
		final var actionContext = context.get(LycheeContextKey.ACTION).newContext();
		actionContext.appendActions(context, assemblingActions.stream(), 1);
		actionContext.run(context);
		return context.getItem(context.size() - 1);
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return CraftingRecipe.super.recipeBookCategory();
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		var items = shaped.getRemainingItems(input);
		var context = updateContextAndGet(input, null);
		if (context == null) {
			Lychee.LOGGER.warn("Failed to get context for recipe {} when getting remaining items", input.items());
			return items;
		}
		applyPostActions(context, 1);
		var craftingContext = context.get(LycheeContextKey.CRAFTING);
		var itemStackHolders = context.get(LycheeContextKey.ITEM);
//		Lychee.LOGGER.info(
//				"isClientSide {} height {} width {}: {}",
//				context.level().isClientSide(),
//				getHeight(),
//				getWidth(),
//				itemStackHolders.stream().map(ExtendedItemStackHolder::get).toList());
//		Lychee.LOGGER.error("", new RuntimeException("Stack trace"));
		var k = 0;
		for (var i = 0; i < getHeight(); i++) {
			for (var j = 0; j < getWidth(); j++) {
//				Lychee.LOGGER.info(
//						"item {}: consumption {} item {}",
//						k,
//						itemStackHolders.get(k).getConsumption(),
//						itemStackHolders.get(k).get());
				if (itemStackHolders.get(k).getConsumption() == 0) {
					items.set(input.width() * i + (craftingContext.mirror() ? getWidth() - j - 1 : j), context.getItem(k));
				}
				++k;
			}
		}
		return items;
	}

	@Override
	public @Nullable ActionContext applyPostActions(LycheeContext context, int times) {
		final var actionContext = context.get(LycheeContextKey.ACTION).newContext();
		actionContext.appendActions(context, postActions().stream(), 1);
		actionContext.run(context);
		return actionContext;
	}

	@Nullable
	public LycheeContext updateContextAndGet(CraftingInput input, @Nullable Level newLevel) {
		InputKey key = new InputKey(input);
		var resolvableContext = CONTEXT_CACHE.getIfPresent(key);
		if (resolvableContext == null) {
			if (newLevel == null) {
				return null;
			}
			injectContext(null, input);
			resolvableContext = CONTEXT_CACHE.getIfPresent(key);
			if (resolvableContext == null) {
				return null;
			}
		}
		var context = resolvableContext.resolve(newLevel);
		if (context == null) {
			return null;
		}
		var craftingContext = context.getOrNull(LycheeContextKey.CRAFTING);
		if (craftingContext != null) {
			return context;
		}
		context.put(LycheeContextKey.RECIPE, this);
		var mirror = false;
		var matched = false;
		final var shapedRecipeAccess = (ShapedRecipeAccess) shaped;
		final var pattern = (ShapedRecipePatternAccess) (Object) shapedRecipeAccess.getPattern();
		if (pattern.ingredientCount() != input.ingredientCount()
				|| shapedRecipeAccess.getPattern().width() != input.width()
				|| shapedRecipeAccess.getPattern().height() != input.height()) {
			return null;
		}
		if (getWidth() > 1 && pattern.callMatches(input, false)) {
			matched = true;
		} else if (pattern.callMatches(input, true)) {
			matched = true;
			mirror = true;
		}
		if (!matched) {
			return null;
		}
		craftingContext = new CraftingContext(context, input, mirror);
		context.put(LycheeContextKey.CRAFTING, craftingContext);

		if (test(context)) {
			final var result = result().create();
			final var ingredients = getIngredients();
			final var items = new ItemStack[ingredients.size() + 1];
			var k = 0;
			for (var i = 0; i < getHeight(); i++) {
				for (var j = 0; j < getWidth(); j++) {
					items[k] = input.getItem(input.width() * i + (craftingContext.mirror() ? getWidth() - j - 1 : j));
					if (!items[k].isEmpty()) {
						items[k] = items[k].copy();
						items[k].setCount(1);
					}
					++k;
				}
			}
			items[ingredients.size()] = result;
			context.put(LycheeContextKey.ITEM, ItemStackHolderCollection.Inventory.of(context, items));
		}
		return context;
	}

	@Override
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return RecipeSerializers.CRAFTING;
	}

	@Override
	public RecipeType<CraftingRecipe> getType() {
		return RecipeType.CRAFTING;
	}

	@Override
	public CraftingBookCategory category() {
		return shaped.category();
	}

	@Override
	public String group() {
		return shaped.group();
	}

	@Override
	public boolean showNotification() {return shaped.showNotification();}

	public int getWidth() {
		return shaped.getWidth();
	}

	public int getHeight() {
		return shaped.getHeight();
	}

	@Override
	public boolean isSpecial() {
		return shaped.isSpecial();
	}

	public List<PostAction> assemblingActions() {
		return assemblingActions;
	}

	public ShapedRecipe shaped() {
		return shaped;
	}

	public ItemStackTemplate result() {
		return ((ShapedRecipeAccess) shaped).getResult();
	}

	public static final MapCodec<ShapedCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(ILycheeRecipe::commonProperties),
					ShapedRecipe.SERIALIZER.codec()
							.forGetter(ShapedCraftingRecipe::shaped),
					PostAction.LIST_CODEC.optionalFieldOf("assembling", List.of())
							.forGetter(ShapedCraftingRecipe::assemblingActions)
			).apply(instance, ShapedCraftingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ShapedCraftingRecipe> STREAM_CODEC =
			StreamCodec.composite(
					LycheeRecipeCommonProperties.STREAM_CODEC,
					ShapedCraftingRecipe::commonProperties,
					// Do NOT use RecipeSerializer.SHAPED_RECIPE.streamCodec(), missing data
					ByteBufCodecs.fromCodecWithRegistries(ShapedRecipe.SERIALIZER.codec().codec()),
					ShapedCraftingRecipe::shaped,
					PostAction.STREAM_LIST_CODEC,
					ShapedCraftingRecipe::assemblingActions,
					ShapedCraftingRecipe::new);
}
