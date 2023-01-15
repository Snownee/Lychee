package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_burning.ItemBurningRecipe;

public class ItemBurningRecipeCategory extends ItemAndBlockBaseCategory<LycheeContext, ItemBurningRecipe> {

	public ItemBurningRecipeCategory(LycheeRecipeType<LycheeContext, ItemBurningRecipe> recipeType) {
		super(List.of(recipeType), AllGuiTextures.JEI_DOWN_ARROW);
		methodRect.setX(27);
	}

	@Override
	public BlockState getIconBlock(List<ItemBurningRecipe> recipes) {
		return Blocks.FIRE.defaultBlockState();
	}

	@Nullable
	@Override
	public BlockPredicate getInputBlock(ItemBurningRecipe recipe) {
		return null;
	}

	@Override
	public BlockState getRenderingBlock(ItemBurningRecipe recipe) {
		return getIconBlock(List.of(recipe));
	}

}
