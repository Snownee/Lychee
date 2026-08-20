package snownee.lychee.mixin.recipes.crafting;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import snownee.lychee.recipes.ShapedCraftingRecipe;

@Mixin(CraftingContainer.class)
public interface CraftingContainerMixin {
	@WrapMethod(method = "asCraftInput")
	private CraftingInput asCraftInput(Operation<CraftingInput> original) {
		CraftingInput result = original.call();
		ShapedCraftingRecipe.injectContext((CraftingContainer) this, result);
		return result;
	}

	@WrapMethod(method = "asPositionedCraftInput")
	private CraftingInput.Positioned asPositionedCraftInput(Operation<CraftingInput.Positioned> original) {
		CraftingInput.Positioned result = original.call();
		ShapedCraftingRecipe.injectContext((CraftingContainer) this, result.input());
		return result;
	}
}
