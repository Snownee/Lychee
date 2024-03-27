package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.crafting.Ingredient;

@Mixin(Ingredient.class)
public interface IngredientAccess {
	@Invoker("<init>")
	static Ingredient construct(Ingredient.Value... values) {
		throw new IllegalStateException();
	}

	@Accessor
	Ingredient.Value[] getValues();
}
