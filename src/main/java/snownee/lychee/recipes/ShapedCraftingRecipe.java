package snownee.lychee.recipes;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
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

public class ShapedCraftingRecipe extends LycheeRecipe<ShapedCraftingRecipe> {
	protected final ShapedRecipe shaped;
	protected final List<PostAction<?>> assemblingActions;

	public ShapedCraftingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			final ShapedRecipe shaped,
			final List<PostAction<?>> assemblingActions
	) {
		super(commonProperties);
		this.assemblingActions = assemblingActions;
		this.shaped = shaped;
	}

	public ShapedCraftingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			final String group,
			final CraftingBookCategory category,
			final ShapedRecipePattern pattern,
			final ItemStack result,
			final boolean showNotification,
			final List<PostAction<?>> assemblingActions
	) {
		super(commonProperties);
		this.assemblingActions = assemblingActions;
		this.shaped = new ShapedRecipe(group, category, pattern, result, showNotification);
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		if (ghost()) {
			return false;
		}
		/* TODO 需要在 mixin 里初始化这个上下文
		 * final var craftingContext = context.put(
		 * 		LycheeContextKey.CRAFTING,
		 * 		CraftingContext.make(container, context, i, j, mirror)
		 * );
		 */
		final var craftingContext = context.get(LycheeContextKey.CRAFTING);
		final var container = craftingContext.container();
		if (level.isClientSide) {
			return shaped.matches(container, level);
		}
		final var shapedRecipeAccess = (ShapedRecipeAccess) shaped;
		final var pattern = (ShapedRecipePatternAccess) (Object) shapedRecipeAccess.getPattern();
		var i = 0;
		var j = 0;
		var mirror = false;
		var matched = false;
		outer:
		for (i = 0; i <= container.getWidth() - getWidth(); ++i) {
			for (j = 0; j <= container.getHeight() - getHeight(); ++j) {
				if (pattern.callMatches(container, i, j, true)) {
					matched = true;
					break outer;
				}
				if (getWidth() > 1 && pattern.callMatches(container, i, j, false)) {
					matched = true;
					mirror = true;
					break outer;
				}
			}
		}
		if (!matched) {
			return false;
		}

		matched = conditions().test(this, context, 1) > 0;
		if (matched) {
			final var result = getResultItem(level.registryAccess()).copy();
			final var ingredients = getIngredients();
			final var items = new ItemStack[ingredients.size() + 1];
			final var startIndex = container.getWidth() * craftingContext.matchY() + craftingContext.matchX();
			var k = 0;
			for (i = 0; i < getHeight(); i++) {
				for (j = 0; j < getWidth(); j++) {
					items[k] = container.getItem(
							startIndex + container.getWidth() * k + (craftingContext.mirror() ? getWidth() - j : j));
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
		return matched;
	}

	@Override
	public @NotNull ItemStack assemble(final LycheeContext context, final RegistryAccess registryAccess) {
		final var craftingContext = context.get(LycheeContextKey.CRAFTING);
		if (craftingContext == null) {
			return ItemStack.EMPTY;
		}
		final var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.reset();
		actionContext.jobs.addAll(assemblingActions.stream().map(it -> new Job(it, 1)).toList());
		actionContext.run(this, context);
		return context.getItem(context.getContainerSize() - 1);
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		// TODO
		return null;
	}

	@Override
	public @NotNull RecipeType<? extends ILycheeRecipe<ShapedCraftingRecipe>> getType() {
		// TODO
		return null;
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
		return shaped.canCraftInDimensions(
				width,
				height
		);
	}

	public int getWidth() {return shaped.getWidth();}

	public int getHeight() {return shaped.getHeight();}

	@Override
	public boolean isIncomplete() {return shaped.isIncomplete();}

	public NonNullList<ItemStack> getRemainingItems(final CraftingContainer container) {
		return shaped.getRemainingItems(container);
	}

	@Override
	public boolean isSpecial() {return shaped.isSpecial();}

	@Override
	public @NotNull ItemStack getToastSymbol() {return shaped.getToastSymbol();}

	public static class Serializer implements LycheeRecipeSerializer<ShapedCraftingRecipe> {
		public static final Codec<ShapedCraftingRecipe> CODEC =
				RecordCodecBuilder.create(instance -> instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(ILycheeRecipe::commonProperties),
						RecipeSerializer.SHAPED_RECIPE.codec()
								.dispatchMap(it -> it, it -> it.getSerializer().codec())
								.forGetter(it -> it.shaped),
						PostActionType.LIST_CODEC.optionalFieldOf("assemblingActions", List.of())
								.forGetter(AnvilCraftingRecipe::assemblingActions)
				).apply(instance, ShapedCraftingRecipe::new));

		@Override
		public @NotNull Codec<ShapedCraftingRecipe> codec() {
			return CODEC;
		}
	}
}
