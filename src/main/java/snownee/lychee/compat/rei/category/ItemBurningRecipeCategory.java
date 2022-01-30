package snownee.lychee.compat.rei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.display.ItemBurningDisplay;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_burning.ItemBurningRecipe;

public class ItemBurningRecipeCategory extends ItemAndBlockBaseCategory<LycheeContext, ItemBurningRecipe, ItemBurningDisplay> {

	public ItemBurningRecipeCategory(LycheeRecipeType<LycheeContext, ItemBurningRecipe> recipeType) {
		super(List.of(recipeType), AllGuiTextures.JEI_DOWN_ARROW);
	}

	@Override
	public BlockState getIconBlock() {
		return Blocks.FIRE.defaultBlockState();
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

	@Override
	public CategoryIdentifier<? extends ItemBurningDisplay> getCategoryIdentifier() {
		return REICompat.ITEM_BURNING;
	}

}
