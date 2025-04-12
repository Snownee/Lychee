package snownee.lychee.compat.recipeviewer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

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
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.util.ui.CategoryMetadata;
import snownee.lychee.util.ui.CategoryModifier;
import snownee.lychee.util.ui.CategorySettingRecipe;
import snownee.lychee.util.ui.InputAction;

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

	public abstract boolean doAction(ItemStack stack, InputAction.Direct action);

	public boolean doAction(Block block, InputAction.Direct action) {
		return doAction(block.asItem().getDefaultInstance(), action);
	}

	public boolean doAction(BlockState blockState, InputAction.Direct action) {
		if (blockState.is(Blocks.CHIPPED_ANVIL) || blockState.is(Blocks.DAMAGED_ANVIL)) {
			blockState = Blocks.ANVIL.defaultBlockState();
		}
		if (blockState.getBlock() instanceof LiquidBlock) {
			return doAction(blockState.getFluidState(), action);
		} else {
			return doAction(blockState.getBlock(), action);
		}
	}

	public abstract boolean doAction(Fluid fluid, InputAction.Direct action);

	public boolean doAction(FluidState fluidState, InputAction.Direct action) {
		return doAction(fluidState.getType(), action);
	}

	private Optional<InputAction.Direct> toDirectAction(InputAction action, @Nullable InteractiveRenderElement element) {
		if (!action.isMouseOver(element)) {
			return Optional.empty();
		}
		return toDirectAction(action);
	}

	public abstract Optional<InputAction.Direct> toDirectAction(InputAction action);

	public BiPredicate<InputAction, InteractiveRenderElement> inputOnBlock(Supplier<BlockState> blockStateSupplier) {
		return (action, element) -> toDirectAction(action, element)
				.map(direct -> doAction(blockStateSupplier.get(), direct))
				.orElse(false);
	}
}
