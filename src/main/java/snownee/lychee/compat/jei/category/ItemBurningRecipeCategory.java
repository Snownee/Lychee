package snownee.lychee.compat.jei.category;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.RecipeTypes;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

@NotNullByDefault
public class ItemBurningRecipeCategory extends ItemAndBlockBaseCategory<ItemBurningRecipe> {

	public ItemBurningRecipeCategory(RecipeType<RecipeHolder<ItemBurningRecipe>> recipeType, IDrawable icon) {
		super(recipeType, icon, RecipeTypes.ITEM_BURNING);
		methodRect.setX(27);
	}

	@Override
	public BlockPredicate getInputBlock(ItemBurningRecipe recipe) {
		return BlockPredicateExtensions.ANY;
	}

	@Override
	public BlockState getRenderingBlock(ItemBurningRecipe recipe) {
		return Blocks.FIRE.defaultBlockState();
	}

	@Override
	protected boolean needRenderInputBlock(ItemBurningRecipe recipe) {
		return false;
	}
}
