package snownee.lychee.compat.rei.display;

import java.util.List;
import java.util.Optional;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;

public abstract class BaseREIDisplay<T extends LycheeRecipe<?>> implements Display {

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

	public static EntryIngredient postAction(PostAction action) {
		List<ItemStack> items = action.getOutputItems();
		if (items.isEmpty()) {
			return EntryIngredient.of(EntryStack.of(REICompat.POST_ACTION, action));
		} else {
			return EntryIngredients.ofItemStacks(items);
		}
	}

	public static void addBlockInputs(List<EntryIngredient> items, BlockPredicate block) {
		if (block == null)
			return;
		List<ItemStack> items1 = BlockPredicateHelper.getMatchedItemStacks(block);
		if (!items1.isEmpty()) {
			items.add(EntryIngredients.ofItemStacks(items1));
		}
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return EntryIngredients.ofIngredients(recipe.getIngredients());
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return recipe.getShowingPostActions().stream().map(BaseREIDisplay::postAction).toList();
	}

	@Override
	public Optional<ResourceLocation> getDisplayLocation() {
		return Optional.of(recipe.getId());
	}

}
