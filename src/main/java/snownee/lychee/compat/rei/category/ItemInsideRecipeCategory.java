package snownee.lychee.compat.rei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.display.ItemInsideDisplay;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.util.Pair;

public class ItemInsideRecipeCategory<T extends ItemAndBlockRecipe<LycheeContext>> extends ItemAndBlockBaseCategory<LycheeContext, T, ItemInsideDisplay<T>> {

	private BlockState iconBlock;

	public ItemInsideRecipeCategory(BlockKeyRecipeType<LycheeContext, T> recipeType, ScreenElement mainIcon) {
		this(List.of(recipeType), mainIcon);
	}

	@SuppressWarnings("rawtypes")
	public ItemInsideRecipeCategory(List<BlockKeyRecipeType<LycheeContext, T>> recipeTypes, ScreenElement mainIcon) {
		super((List) recipeTypes, mainIcon);
	}

	@Override
	public BlockState getIconBlock() {
		if (iconBlock == null) {
			ClientPacketListener con = Minecraft.getInstance().getConnection();
			if (con == null) {
				return Blocks.AIR.defaultBlockState();
			}
			/* off */
			iconBlock = recipeTypes.stream()
					.map($ -> ((BlockKeyRecipeType<?, ?>) $).getMostUsedBlock())
					.max((a, b) -> a.getSecond() - b.getSecond())
					.map(Pair::getFirst)
					.orElse(Blocks.AIR.defaultBlockState());
			/* on */
		}
		return iconBlock;
	}

	@Override
	@Nullable
	public BlockPredicate getInputBlock(T recipe) {
		return recipe.getBlock();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CategoryIdentifier<? extends ItemInsideDisplay<T>> getCategoryIdentifier() {
		return (CategoryIdentifier) REICompat.ITEM_INSIDE;
	}

}
