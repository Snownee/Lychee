package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.RecipeBookCategories;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.contextual.ContextualPredicate;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.predicates.BlockPredicateExtensions;


public interface ILycheeRecipe<C extends RecipeInput> extends Recipe<C>, ContextualPredicate, Contextual {
	String DEFAULT_GROUP = "default";
	String ITEM_IN = "item_in";
	String ITEM_OUT = "item_out";
	String BLOCK_IN = "block_in";
	JsonPointer ITEM_IN_POINTER = new JsonPointer("/item_in");
	JsonPointer ITEM_OUT_POINTER = new JsonPointer("/item_out");
	JsonPointer RESULT_POINTER = new JsonPointer("/result");
	JsonPointer POST_POINTER = new JsonPointer("/post");
	PlacementInfo DEFAULT_PLACEMENT_INFO = new PlacementInfo(List.of(), IntList.of()) {
		@Override
		public boolean isImpossibleToPlace() {
			return false;
		}
	};

	default void onConstructed() {
		allActions().forEach(it -> it.validate(this));
	}

	default IntList getItemIndexes(Reference reference) {
		JsonPointer pointer = null;
		if (reference == Reference.DEFAULT) {
			pointer = defaultItemPointer();
		} else if (reference.isPointer()) {
			pointer = reference.getPointer();
		}
		if (pointer != null) {
			return getItemIndexes(pointer);
		}
		return IntList.of();
	}

	default IntList getItemIndexes(JsonPointer pointer) {
		int size;
		try {
			size = sizedIngredients().size();
		} catch (Exception ignored) {
			size = ingredientCount();
		}
		if (pointer.size() == 1 && pointer.getString(0).equals(ITEM_IN)) {
			return IntList.of(IntStream.range(0, size).toArray());
		}
		if (pointer.size() == 2 && pointer.getString(0).equals(ITEM_IN)) {
			try {
				return IntList.of(pointer.getInt(1));
			} catch (NumberFormatException ignored) {
			}
		}
		return IntList.of();
	}

	@Override
	default ItemStack assemble(C inv) {
		return ItemStack.EMPTY;
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.UNLISTED;
	}

	@Override
	default PlacementInfo placementInfo() {
		return DEFAULT_PLACEMENT_INFO;
	}

	default JsonPointer defaultItemPointer() {
		return ITEM_IN_POINTER;
	}

	default boolean tickOrApply(LycheeContext context) {
		return true;
	}

	@Override
	boolean matches(C context, Level level);

	@Override
	RecipeType<? extends Recipe<C>> getType();

	LycheeRecipeCommonProperties commonProperties();

	@Override
	default ContextualHolder conditions() {
		return commonProperties().conditions();
	}

	default boolean test(LycheeContext ctx) {
		return test(ctx, ctx.get(LycheeContextKey.ACTION).prototype(), 1) == 1;
	}

	@Override
	default int test(LycheeContext ctx, ActionContext actionContext, int times) {
		return conditions().test(ctx, actionContext, times);
	}

	default boolean hideInRecipeViewer() {
		return commonProperties().hideInRecipeViewer();
	}

	default boolean ghost() {
		return commonProperties().ghost();
	}

	default Optional<String> comment() {
		return commonProperties().comment();
	}

	@Override
	default String group() {
		return commonProperties().group();
	}

	default List<PostAction> postActions() {
		return commonProperties().postActions();
	}

	default MinMaxBounds.Ints maxRepeats() {
		return commonProperties().maxRepeats();
	}

	default int getRandomRepeats(int max, LycheeContext ctx) {
		if (maxRepeats() == BoundsExtensions.ONE) {
			return 1;
		}
		int times = Integer.MAX_VALUE;
		if (!maxRepeats().isAny()) {
			times = BoundsExtensions.random(maxRepeats(), ctx.get(LycheeContextKey.RANDOM));
		}
		return Mth.clamp(times, 1, max);
	}

	default Stream<PostAction> allActions() {
		return postActions().stream();
	}

	default @Nullable ActionContext applyPostActions(LycheeContext context, int times) {
		if (context.level().isClientSide()) {
			return null;
		}
		var actionManager = context.get(LycheeContextKey.ACTION);
		var actionContext = actionManager.newContext();
		actionContext.jobs.addAll(postActions().stream().map(it -> new Job(it, times)).toList());
		actionContext.run(context);
		return actionContext;
	}

	default List<BlockPredicate> getBlockInputs() {
		if (this instanceof BlockKeyableRecipe blockPredicateRecipe
				&& !BlockPredicateExtensions.isAny(blockPredicateRecipe.blockPredicate())) {
			return List.of(blockPredicateRecipe.blockPredicate());
		}
		return List.of();
	}

	default List<BlockPredicate> getBlockOutputs() {
		return allActions()
				.filter(it -> !it.hidden())
				.map(PostAction::getOutputBlocks)
				.flatMap(List::stream)
				.toList();
	}

	@Nullable
	default IngredientCollection ingredientCollection() {
		return null;
	}

	default List<SizedIngredient> sizedIngredients() {
		IngredientCollection collection = ingredientCollection();
		if (collection == null) {
			throw new UnsupportedOperationException();
		}
		return collection.ingredients();
	}

	default List<Ingredient> getIngredients() {
		IngredientCollection collection = ingredientCollection();
		if (collection == null) {
			return List.of();
		}
		return collection.flattenedIngredients();
	}

	int ingredientCount();
}
