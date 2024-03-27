package snownee.lychee.compat.rei.display;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import snownee.lychee.util.action.PostActionDisplay;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface LycheeDisplay<T extends ILycheeRecipe<?>> extends Display {
	static List<EntryIngredient> getInputEntries(ILycheeRecipe<?> recipe) {
		var ingredients = Lists.newArrayList(EntryIngredients.ofIngredients(recipe.getIngredients()));
		recipe.getBlockInputs().stream()
				.map(BlockPredicateExtensions::matchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map(it -> EntryIngredients.of(FluidStack.create(it, FluidStack.bucketAmount())))
				.forEach(ingredients::add);
		recipe.getBlockInputs().stream()
				.map(BlockPredicateExtensions::matchedBlocks)
				.flatMap(Set::stream)
				.map(Block::asItem)
				.filter(it -> !it.equals(Items.AIR))
				.distinct()
				.map(EntryIngredients::of)
				.forEach(ingredients::add);
		return ingredients;
	}

	static List<EntryIngredient> getOutputEntries(ILycheeRecipe<?> recipe) {
		var ingredients = Lists.<EntryIngredient>newArrayList();
		recipe.allActions().filter(it -> !it.hidden())
				.map(PostActionDisplay::getOutputItems)
				.flatMap(List::stream)
				.map(EntryIngredients::of)
				.forEach(ingredients::add);
		recipe.getBlockOutputs().stream()
				.map(BlockPredicateExtensions::matchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map(it -> EntryIngredients.of(FluidStack.create(it, FluidStack.bucketAmount())))
				.forEach(ingredients::add);
		return ingredients;
	}

	T recipe();

	@Override
	default List<EntryIngredient> getInputEntries() {
		return getInputEntries(recipe());
	}

	@Override
	default List<EntryIngredient> getOutputEntries() {
		return getOutputEntries(recipe());
	}
}
