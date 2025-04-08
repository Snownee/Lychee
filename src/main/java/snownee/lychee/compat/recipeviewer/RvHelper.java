package snownee.lychee.compat.recipeviewer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import snownee.kiwi.util.KUtil;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.util.ui.CategoryMetadata;
import snownee.lychee.util.ui.CategoryModifier;
import snownee.lychee.util.ui.CategorySettingRecipe;

public abstract class RvHelper {
	private List<RecipeHolder<CategoryMetadata>> metadataList = List.of();
	private List<RecipeHolder<CategoryModifier>> modifierList = List.of();

	public void init() {
		Comparator<RecipeHolder<? extends CategorySettingRecipe>> comparator = Comparator.comparing(RecipeHolder::value);
		metadataList = ImmutableList.sortedCopyOf(comparator, KUtil.getRecipes(RecipeTypes.CATEGORY_METADATA));
		modifierList = ImmutableList.sortedCopyOf(comparator, KUtil.getRecipes(RecipeTypes.CATEGORY_MODIFIER));
	}

	public RecipeHolder<CategoryMetadata> getMetadata(RvCategoryInstance<?> category) {
		String id = category.id().toString();
		for (RecipeHolder<CategoryMetadata> metadata : metadataList) {
			if (metadata.value().category().test(id)) {
				return metadata;
			}
		}
		return CategoryMetadata.EMPTY;
	}

	public List<RecipeHolder<CategoryModifier>> getModifiers(RvCategoryInstance<?> category) {
		String id = category.id().toString();
		List<RecipeHolder<CategoryModifier>> list = Lists.newArrayList();
		for (RecipeHolder<CategoryModifier> modifier : modifierList) {
			if (modifier.value().category().test(id)) {
				list.add(modifier);
			}
		}
		return ImmutableList.copyOf(list);
	}

	/**
	 * @param stack         The stack to open
	 * @param usageOrRecipe true for usage, false for recipes
	 * @return the page is opened
	 */
	public abstract boolean openPage(ItemStack stack, boolean usageOrRecipe);

	public boolean openPage(Block block, boolean usageOrRecipe) {
		return openPage(block.asItem().getDefaultInstance(), usageOrRecipe);
	}

	public boolean openPage(BlockState blockState, boolean usageOrRecipe) {
		if (blockState.is(Blocks.CHIPPED_ANVIL) || blockState.is(Blocks.DAMAGED_ANVIL)) {
			blockState = Blocks.ANVIL.defaultBlockState();
		}
		if (blockState.getBlock() instanceof LiquidBlock) {
			return openPage(blockState.getFluidState(), usageOrRecipe);
		} else {
			return openPage(blockState.getBlock(), usageOrRecipe);
		}
	}

	public abstract boolean openPage(Fluid fluid, boolean usageOrRecipe);

	public boolean openPage(FluidState fluidState, boolean usageOrRecipe) {
		return openPage(fluidState.getType(), usageOrRecipe);
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

	public IntPredicate lookupBlock(Supplier<BlockState> blockStateSupplier) {
		return button -> buttonToUsageOrRecipe(button)
				.map(usageOrRecipe -> openPage(blockStateSupplier.get(), usageOrRecipe))
				.orElse(false);
	}
}
