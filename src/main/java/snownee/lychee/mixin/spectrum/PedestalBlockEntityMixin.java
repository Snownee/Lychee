package snownee.lychee.mixin.spectrum;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import de.dafuqs.spectrum.blocks.pedestal.PedestalBlockEntity;
import de.dafuqs.spectrum.blocks.pedestal.PedestalRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.recipes.ShapedCraftingRecipe;

@Mixin(PedestalBlockEntity.class)
public abstract class PedestalBlockEntityMixin extends BaseContainerBlockEntity {
	protected PedestalBlockEntityMixin(
			BlockEntityType<?> type,
			BlockPos pos,
			BlockState blockState) {
		super(type, pos, blockState);
	}

	@WrapMethod(method = "createRecipeInput", remap = false)
	private PedestalRecipeInput createRecipeInput(Operation<PedestalRecipeInput> original) {
		PedestalRecipeInput result = original.call();
		ShapedCraftingRecipe.injectContext(this, result.getCraftingGridInput());
		return result;
	}

	@WrapMethod(method = "createPositionedInput", remap = false)
	private CraftingInput.Positioned createPositionedInput(Operation<CraftingInput.Positioned> original) {
		CraftingInput.Positioned result = original.call();
		ShapedCraftingRecipe.injectContext(this, result.input());
		return result;
	}
}
