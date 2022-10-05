package snownee.lychee.compat.rei.display;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.advancements.critereon.BlockPredicate;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.item_burning.ItemBurningRecipe;

public class ItemBurningDisplay extends ItemAndBlockBaseDisplay<ItemBurningRecipe> {

	public ItemBurningDisplay(ItemBurningRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.ITEM_BURNING;
	}

	@Override
	public @Nullable BlockPredicate getInputBlock(ItemBurningRecipe recipe) {
		return null;
	}

}
