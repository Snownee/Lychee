package snownee.lychee.compat.recipeviewer.category;

import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.recipes.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {

	@Override
	public void setupDecorations(DecorationMapBuilder<ItemExplodingRecipe> mapBuilder) {
		super.setupDecorations(mapBuilder);
		mapBuilder.condition("icon", $ -> false);

		mapBuilder.put(
				"tnt", (builder, recipeHolder) -> {
					builder.addElement(RenderElement.create(RVs::renderTnt).at((float) builder.width() / 2, 38));
				});
	}
}
