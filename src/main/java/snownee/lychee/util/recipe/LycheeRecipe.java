package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.core.Reference;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.json.JsonPointer;

public interface LycheeRecipe<T extends LycheeRecipe<T>> extends Recipe<LycheeContext>, Contextual<T> {
	String DEFAULT_GROUP = "default";
	String ITEM_IN = "item_in";
	String ITEM_OUT ="item_out";
	JsonPointer ITEM_IN_POINTER = new JsonPointer("/item_in");
	JsonPointer ITEM_OUT_POINTER = new JsonPointer("/item_out");
	JsonPointer RESULT_POINTER = new JsonPointer("/result");
	JsonPointer POST_POINTER = new JsonPointer("/post");

	default ResourceLocation id() {
		//noinspection SimplifyStreamApiCallChains
		return CommonProxy.recipes(getType())
						  .stream()
						  .filter(it -> it.value().equals(this))
						  .collect(Collectors.reducing((a, b) -> null))
						  .orElseThrow()
						  .id();
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

	IntList getItemIndexes(JsonPointer pointer);

	default JsonPointer defaultItemPointer() {
		return ITEM_IN_POINTER;
	}

	@Override
	boolean matches(LycheeContext context, Level level);

	@Override
	@NotNull
	ItemStack assemble(LycheeContext context, RegistryAccess registryAccess);

	@Override
	default boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@NotNull RecipeType<? extends LycheeRecipe<T>> getType();

	boolean hideInRecipeViewer();

	boolean ghost();

	Optional<String> comment();

	String group();

	List<PostAction<?>> postActions();

	MinMaxBounds.Ints maxRepeats();

	default List<PostAction<?>> allActions() {
		return postActions();
	}

	default void applyPostActions(LycheeContext context, int times) {
		if (!context.get(LycheeContextType.GENERIC).level().isClientSide) {
			final var actionContext = context.get(LycheeContextType.ACTION);
			actionContext.reset();
			actionContext.jobs.addAll(postActions().stream().map(it -> new Job(it, times)).toList());
			actionContext.run(new RecipeHolder<>(id(), this), context);
		}
	}

	default List<BlockPredicate> getBlockInputs() {
		if (this instanceof BlockInputLycheeRecipe<?> blockPredicateRecipe) {
			return List.of(blockPredicateRecipe.blockPredicate());
		}
		return List.of();
	}

	default List<BlockPredicate> getBlockOutputs() {
		return allActions().stream()
						   .filter(it -> !it.hidden())
						   .map(PostAction::getOutputBlocks)
						   .flatMap(List::stream)
						   .toList();
	}
}
