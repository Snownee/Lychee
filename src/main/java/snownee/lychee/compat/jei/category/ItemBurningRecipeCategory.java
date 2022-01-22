package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_burning.ItemBurningRecipe;

public class ItemBurningRecipeCategory extends ItemAndBlockBaseCategory<LycheeContext, ItemBurningRecipe> {

	public ItemBurningRecipeCategory(LycheeRecipeType<LycheeContext, ItemBurningRecipe> recipeType, IGuiHelper guiHelper) {
		super(List.of(recipeType), guiHelper, AllGuiTextures.JEI_DOWN_ARROW);
	}

	@Override
	public BlockState getIconBlock() {
		return Blocks.FIRE.defaultBlockState();
	}

	@Override
	public void setInputs(ItemBurningRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(List.of(recipe.getInput()));
	}

	@Nullable
	@Override
	public BlockPredicate getInputBlock(ItemBurningRecipe recipe) {
		return null;
	}

	@Override
	public BlockState getRenderingBlock(ItemBurningRecipe recipe) {
		return getIconBlock();
	}

}
