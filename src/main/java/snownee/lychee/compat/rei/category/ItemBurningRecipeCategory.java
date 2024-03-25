package snownee.lychee.compat.rei.category;

import java.util.Optional;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.recipes.ItemBurningRecipe;

public class ItemBurningRecipeCategory extends ItemAndBlockBaseCategory<ItemBurningRecipe> {


	public ItemBurningRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<ItemBurningRecipe>> id,
			Renderer icon
	) {
		super(id, icon, RecipeTypes.ITEM_BURNING);
		methodRect.setX(27);
	}

	@Override
	public Optional<BlockPredicate> getInputBlock(ItemBurningRecipe recipe) {
		return Optional.empty();
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
