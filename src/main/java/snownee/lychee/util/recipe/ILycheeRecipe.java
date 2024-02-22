package snownee.lychee.util.recipe;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.core.Reference;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.contextual.ContextualPredicate;
import snownee.lychee.util.json.JsonPointer;

public interface ILycheeRecipe<C extends Container> extends Recipe<C>, ContextualPredicate, Contextual {
	String DEFAULT_GROUP = "default";
	String ITEM_IN = "item_in";
	String ITEM_OUT = "item_out";
	String BLOCK_IN = "block_in";
	JsonPointer ITEM_IN_POINTER = new JsonPointer("/item_in");
	JsonPointer ITEM_OUT_POINTER = new JsonPointer("/item_out");
	JsonPointer RESULT_POINTER = new JsonPointer("/result");
	JsonPointer POST_POINTER = new JsonPointer("/post");

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
		int size = getIngredients().size();
		if (pointer.size() == 1 && pointer.getString(0).equals("item_in")) {
			return IntList.of(IntStream.range(0, size).toArray());
		}
		if (pointer.size() == 2 && pointer.getString(0).equals("item_in")) {
			try {
				return IntList.of(pointer.getInt(1));
			} catch (NumberFormatException ignored) {
			}
		}
		return IntList.of();
	}

	@Override
	default @NotNull ItemStack assemble(C inv, RegistryAccess registryAccess) {
		return ItemStack.EMPTY;
	}

	default JsonPointer defaultItemPointer() {
		return ITEM_IN_POINTER;
	}

	@Override
	boolean matches(C context, Level level);

	@Override
	default boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	default @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
		return ItemStack.EMPTY;
	}

	@NotNull RecipeType<? extends Recipe<?>> getType();

	LycheeRecipeCommonProperties commonProperties();

	@Override
	default ContextualHolder conditions() {
		return commonProperties().conditions();
	}

	@Override
	default int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return conditions().test(recipe, ctx, times);
	}

	default boolean hideInRecipeViewer() {
		return commonProperties().hideInRecipeViewer();
	}

	default boolean ghost() {
		return commonProperties().ghost();
	}

	default @Nullable String comment() {
		return commonProperties().comment();
	}

	default String group() {
		return commonProperties().group();
	}

	default List<PostAction<?>> postActions() {
		return commonProperties().postActions();
	}

	default MinMaxBounds.Ints maxRepeats() {
		return commonProperties().maxRepeats();
	}

	default int getRandomRepeats(int max, LycheeContext ctx) {
		int times = Integer.MAX_VALUE;
		if (!maxRepeats().isAny()) {
			times = BoundsExtensions.random(maxRepeats(), ctx.get(LycheeContextKey.RANDOM));
		}
		return Mth.clamp(times, 1, max);
	}

	default Stream<PostAction<?>> allActions() {
		return postActions().stream();
	}

	default void applyPostActions(LycheeContext context, int times) {
		if (!context.get(LycheeContextKey.LEVEL).isClientSide) {
			final var actionContext = context.get(LycheeContextKey.ACTION);
			actionContext.reset();
			actionContext.jobs.addAll(postActions().stream().map(it -> new Job(it, times)).toList());
			actionContext.run(this, context);
		}
	}

	default List<BlockPredicate> getBlockInputs() {
		if (this instanceof BlockKeyableRecipe<?> blockPredicateRecipe
				&& blockPredicateRecipe.blockPredicate().isPresent()) {
			return List.of(blockPredicateRecipe.blockPredicate().get());
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
}
