package snownee.lychee.recipes;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.context.CraftingContext;
import snownee.lychee.mixin.recipes.crafting.ShapedRecipeAccess;
import snownee.lychee.mixin.recipes.crafting.ShapedRecipePatternAccess;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class ShapedCraftingRecipe extends LycheeRecipe<CraftingContainer> implements CraftingRecipe {
	private static final Cache<CraftingContainer, LycheeContext> CONTEXT_CACHE =
			CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();

	protected final ShapedRecipe shaped;
	protected final List<PostAction> assemblingActions;

	public ShapedCraftingRecipe(
			final LycheeRecipeCommonProperties commonProperties, final ShapedRecipe shaped, final List<PostAction> assemblingActions) {
		super(commonProperties);
		this.assemblingActions = assemblingActions;
		this.shaped = shaped;
		onConstructed();
	}

	public ShapedCraftingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			final String group,
			final CraftingBookCategory category,
			final ShapedRecipePattern pattern,
			final ItemStack result,
			final boolean showNotification,
			final List<PostAction> assemblingActions
	) {
		super(commonProperties);
		this.assemblingActions = assemblingActions;
		this.shaped = new ShapedRecipe(group, category, pattern, result, showNotification);
		onConstructed();
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (ghost()) {
			return false;
		}
		if (level.isClientSide) {
			return shaped.matches(container, level);
		}
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		var matchX = 0;
		var matchY = 0;
		var mirror = false;
		var matched = false;
		final var shapedRecipeAccess = (ShapedRecipeAccess) shaped;
		final var pattern = (ShapedRecipePatternAccess) (Object) shapedRecipeAccess.getPattern();
		outer:
		for (matchX = 0; matchX <= container.getWidth() - getWidth(); ++matchX) {
			for (matchY = 0; matchY <= container.getHeight() - getHeight(); ++matchY) {
				if (pattern.callMatches(container, matchX, matchY, true)) {
					matched = true;
					break outer;
				}
				if (getWidth() > 1 && pattern.callMatches(container, matchX, matchY, false)) {
					matched = true;
					mirror = true;
					break outer;
				}
			}
		}
		if (!matched) {
			return false;
		}
		var craftingContext = new CraftingContext(context, container, matchX, matchY, mirror);
		context.put(LycheeContextKey.CRAFTING, craftingContext);

		final var passed = conditions().test(this, context, 1) > 0;

		Pair<Vec3, Player> pair = null;
		try {
			pair = CraftingContext.CONTAINER_WORLD_LOCATOR.get(container.getClass()).apply(container);
		} catch (ExecutionException ignored) {
		}
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		if (pair != null) {
			lootParamsContext.setParam(LootContextParams.ORIGIN, pair.getFirst());
			lootParamsContext.setParam(LootContextParams.THIS_ENTITY, pair.getSecond());
		}

		CONTEXT_CACHE.put(container, context);

		if (passed) {
			final var result = getResultItem(level.registryAccess()).copy();
			final var ingredients = getIngredients();
			final var items = new ItemStack[ingredients.size() + 1];
			final var startIndex = container.getWidth() * craftingContext.matchY() + craftingContext.matchX();
			var k = 0;
			for (var i = 0; i < getHeight(); i++) {
				for (var j = 0; j < getWidth(); j++) {
					items[k] = container.getItem(startIndex + container.getWidth() * k + (craftingContext.mirror() ? getWidth() - j : j));
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
		return passed;
	}

	@Override
	public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		var context = CONTEXT_CACHE.getIfPresent(container);
		if (context == null) {
			return ItemStack.EMPTY;
		}
		final var craftingContext = context.get(LycheeContextKey.CRAFTING);
		if (craftingContext == null) {
			return ItemStack.EMPTY;
		}
		final var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.reset();
		actionContext.jobs.addAll(assemblingActions.stream().map(it -> new Job(it, 1)).toList());
		actionContext.run(context);
		return context.getItem(context.getContainerSize() - 1);
	}

	@Override
	public @NotNull RecipeSerializer<ShapedCraftingRecipe> getSerializer() {
		return RecipeSerializers.CRAFTING;
	}

	@Override
	public @NotNull RecipeType<? extends CraftingRecipe> getType() {
		return RecipeType.CRAFTING;
	}

	@Override
	public @NotNull CraftingBookCategory category() {
		return shaped.category();
	}

	@Override
	public @NotNull String getGroup() {return shaped.getGroup();}

	@NotNull
	@Override
	public ItemStack getResultItem(final RegistryAccess registryAccess) {return shaped.getResultItem(registryAccess);}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {return shaped.getIngredients();}

	@Override
	public boolean showNotification() {return shaped.showNotification();}

	@Override
	public boolean canCraftInDimensions(final int width, final int height) {
		return shaped.canCraftInDimensions(width, height);
	}

	public int getWidth() {return shaped.getWidth();}

	public int getHeight() {return shaped.getHeight();}

	@Override
	public boolean isIncomplete() {return shaped.isIncomplete();}

	public @NotNull NonNullList<ItemStack> getRemainingItems(final CraftingContainer container) {
		return shaped.getRemainingItems(container);
	}

	@Override
	public boolean isSpecial() {return shaped.isSpecial();}

	@Override
	public @NotNull ItemStack getToastSymbol() {return shaped.getToastSymbol();}

	public List<PostAction> assemblingActions() {
		return assemblingActions;
	}

	public ShapedRecipe shaped() {
		return shaped;
	}

	public static class Serializer implements LycheeRecipeSerializer<ShapedCraftingRecipe> {
		public static final Codec<ShapedCraftingRecipe> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(ILycheeRecipe::commonProperties),
						((MapCodec.MapCodecCodec<ShapedRecipe>) RecipeSerializer.SHAPED_RECIPE.codec()).codec()
								.forGetter(ShapedCraftingRecipe::shaped),
						ExtraCodecs.strictOptionalField(PostActionType.LIST_CODEC, "assembling", List.of())
								.forGetter(ShapedCraftingRecipe::assemblingActions)
				).apply(instance, ShapedCraftingRecipe::new));

		@Override
		public @NotNull Codec<ShapedCraftingRecipe> codec() {
			return CODEC;
		}
	}
}
