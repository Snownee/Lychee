package snownee.lychee.compat.recipeviewer;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.util.action.PostAction;

public class IngredientInfo {
	public final Ingredient ingredient;
	public List<Component> tooltips = List.of();
	public @Nullable PostAction relatedAction;
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
