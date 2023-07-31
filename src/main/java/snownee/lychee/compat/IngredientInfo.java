package snownee.lychee.compat;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientInfo {
	public final Ingredient ingredient;
	public List<Component> tooltips = List.of();
	public int count = 1;
	public boolean isCatalyst;

	public IngredientInfo(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public void addTooltip(Component line) {
		if (tooltips.isEmpty()) {
			tooltips = Lists.newArrayList();
		}
		tooltips.add(line);
	}

	public enum Type {
		NORMAL, AIR, ANY
	}
}
