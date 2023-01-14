package snownee.lychee.compat.rei.display;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.advancements.critereon.BlockPredicate;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;

public class ItemAndBlockBaseDisplay<T extends LycheeRecipe<?>> extends BaseREIDisplay<T> {

	public ItemAndBlockBaseDisplay(T recipe, CategoryIdentifier<?> categoryId) {
		super(recipe, categoryId);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		List<EntryIngredient> items = Lists.newArrayList(super.getInputEntries());
		addBlockInputs(items, getInputBlock(recipe));
		return items;
	}

	@Nullable
	public BlockPredicate getInputBlock(T recipe) {
		return ((BlockKeyRecipe<?>) recipe).getBlock();
	}

}
