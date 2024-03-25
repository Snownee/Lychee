package snownee.lychee.compat.rei.category;

import java.util.List;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemShapelessContext, ItemExplodingRecipe, BaseREIDisplay<ItemExplodingRecipe>> {
	public ItemExplodingRecipeCategory(LycheeRecipeType<ItemShapelessContext, ItemExplodingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public void drawExtra(List<Widget> widgets, BaseREIDisplay<ItemExplodingRecipe> display, Rectangle bounds) {
		Widget widget = Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			JEIREI.renderTnt(graphics, bounds.x + 89, bounds.y + 38);
		});
		widgets.add(widget);
	}

	@Override
	public Renderer createIcon(List<ItemExplodingRecipe> recipes) {
		return EntryStacks.of(Items.TNT);
	}

}
