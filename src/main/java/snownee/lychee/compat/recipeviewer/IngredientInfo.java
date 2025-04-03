package snownee.lychee.compat.recipeviewer;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.recipe.SizedIngredient;

public class IngredientInfo {
	public final Ingredient ingredient;
	public List<Component> tooltips = List.of();
	public int count = 1;
	public SlotType type = SlotType.NORMAL;

	public IngredientInfo(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public IngredientInfo(SizedIngredient ingredient) {
		this.ingredient = ingredient.ingredient();
		this.count = ingredient.count();
	}

	public void addTooltip(Component line) {
		if (tooltips.isEmpty()) {
			tooltips = Lists.newArrayList();
		}
		tooltips.add(line);
	}
}
