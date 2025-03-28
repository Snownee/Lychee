package snownee.lychee.compat.recipeviewer;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class RVHelper {
	/**
	 * @param stack         The stack to open
	 * @param usageOrRecipe true for usage, false for recipes
	 * @return the page is opened
	 */
	public abstract boolean openPage(ItemStack stack, boolean usageOrRecipe);

	public boolean openPage(Block block, boolean usageOrRecipe) {
		return openPage(block.asItem().getDefaultInstance(), usageOrRecipe);
	}

	public boolean openPage(BlockState state, boolean usageOrRecipe) {
		if (state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
			state = Blocks.ANVIL.defaultBlockState();
		}
		if (state.getBlock() instanceof LiquidBlock) {
			return openPage(state.getFluidState(), usageOrRecipe);
		} else {
			return openPage(state.getBlock(), usageOrRecipe);
		}
	}

	public abstract boolean openPage(Fluid fluid, boolean usageOrRecipe);

	public boolean openPage(FluidState state, boolean usageOrRecipe) {
		return openPage(state.getType(), usageOrRecipe);
	}

	public Optional<Boolean> buttonToUsageOrRecipe(int button) {
		if (button == 0) {
			return Optional.of(false);
		} else if (button == 1) {
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}
}
