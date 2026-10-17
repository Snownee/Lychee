package snownee.lychee.mixin.recipes.crafting;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

@Mixin(ShapedRecipePattern.class)
public interface ShapedRecipePatternAccess {
	@Invoker
	boolean callMatches(CraftingInput input, boolean symmetrical);

	@Accessor("data")
	Optional<ShapedRecipePattern.Data> data();

	@Accessor("ingredientCount")
	int ingredientCount();
}
