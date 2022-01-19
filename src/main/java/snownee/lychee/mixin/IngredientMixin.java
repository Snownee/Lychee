package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.lychee.core.IngredientType;
import snownee.lychee.core.LycheeIngredient;

@Mixin(Ingredient.class)
public class IngredientMixin implements LycheeIngredient<ItemStack> {

	@Override
	public boolean lychee_test(ItemStack t) {
		return ((Ingredient) (Object) this).test(t);
	}

	@Override
	public IngredientType lychee_getType() {
		return IngredientType.ITEM_STACK;
	}

}
