package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.recipes.ItemExplodingRecipe;

@NotNullByDefault
public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {
	protected ItemExplodingRecipeCategory(
			RvCategoryType<ItemExplodingRecipe> type,
			ResourceLocation id,
			RvHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

	@Override
	public void configureDecorations(
			RvCategoryWidgetBuilder builder,
			RecipeHolder<ItemExplodingRecipe> recipeHolder,
			Vector2fc position
	) {
		super.configureDecorations(builder, recipeHolder, position);
		builder.addElement(RenderElement.create(RVs::renderTnt).at((float) width() / 2, 38).offset(position));
	}

	@Override
	protected boolean needRenderIcon() {
		return false;
	}
}
