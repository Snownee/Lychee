package snownee.lychee.compat.rei.display;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;

public class BaseREIDisplay<T extends LycheeRecipe<?>> implements Display {

	public final T recipe;
	private final CategoryIdentifier<?> categoryId;

	public BaseREIDisplay(T recipe, CategoryIdentifier<?> categoryId) {
		this.recipe = recipe;
		this.categoryId = categoryId;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return categoryId;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		List<EntryIngredient> ingredients = Lists.newArrayList(EntryIngredients.ofIngredients(recipe.getIngredients()));
		/* off */
		recipe.getBlockInputs().stream()
				.map(BlockPredicateHelper::getMatchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map($ -> EntryIngredients.of(FluidStack.create($, FluidStack.bucketAmount())))
				.forEach(ingredients::add);
		recipe.getBlockInputs().stream()
				.map(BlockPredicateHelper::getMatchedBlocks)
				.flatMap(Set::stream)
				.map(ItemLike::asItem)
				.filter(Predicate.not(Items.AIR::equals))
				.distinct()
				.map(EntryIngredients::of)
				.forEach(ingredients::add);
		/* on */
		return ingredients;
	}

	public static List<EntryIngredient> getOutputEntries(ILycheeRecipe<?> recipe) {
		List<EntryIngredient> ingredients = Lists.newArrayList();
		/* off */
		ILycheeRecipe.filterHidden(recipe.getAllActions())
				.map(PostAction::getItemOutputs)
				.flatMap(List::stream)
				.map(EntryIngredients::of)
				.forEach(ingredients::add);
		recipe.getBlockOutputs().stream()
				.map(BlockPredicateHelper::getMatchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map($ -> EntryIngredients.of(FluidStack.create($, FluidStack.bucketAmount())))
				.forEach(ingredients::add);
		/* on */
		return ingredients;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return getOutputEntries(recipe);
	}

	@Override
	public Optional<ResourceLocation> getDisplayLocation() {
		return Optional.of(recipe.getId());
	}

}
