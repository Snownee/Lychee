package snownee.lychee.compat.rv.category;

import org.joml.Vector2i;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.ui.TextElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.VectorExtensions;

public class ItemInsideRecipeCategory extends ItemAndBlockCategory<ItemInsideRecipe> {
	public ItemInsideRecipeCategory(
			RvCategoryType<ItemInsideRecipe> type,
			ResourceLocation id,
			RVCategoryHandler rvHandler
	) {
		super(
				type,
				id,
				rvHandler,
				VectorExtensions.withX(INPUT_BLOCK_POSITION, 80),
				VectorExtensions.withX(METHOD_POSITION, 77),
				VectorExtensions.withX(INGREDIENT_POSITION, 40));
	}

	@Override
	public void configureDecorations(
			RvCategoryWidgetBuilder builder,
			RecipeHolder<ItemInsideRecipe> recipeHolder,
			Vector2i position
	) {
		super.configureDecorations(builder, recipeHolder, position);
		var recipe = recipeHolder.value();
		if (recipe.time() > 0) {
			builder.addElement(new TextElementRenderer(ClientProxy.format("tip.lychee.sec", recipe.time()))
					.color(0xFF666666)
					.centered()
					.shadow());
		}
	}
}
