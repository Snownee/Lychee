package snownee.lychee.core;

import net.minecraft.world.item.ItemStack;

public class IngredientType {

	public static final IngredientType ITEM_STACK = new IngredientType(ItemStack.class);

	@SuppressWarnings("unused")
	private final Class<?> clazz;

	public IngredientType(Class<?> clazz) {
		this.clazz = clazz;
	}

}
