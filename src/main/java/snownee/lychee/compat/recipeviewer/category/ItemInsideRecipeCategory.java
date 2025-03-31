package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.ui.TextElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.VectorExtensions;

public class ItemInsideRecipeCategory extends ItemAndBlockCategory<ItemInsideRecipe> {
	public static final Vector2fc INPUT_BLOCK_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 46);
	public static final Vector2fc METHOD_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.METHOD_POSITION, 46);
	private static final float INPUT_INGREDIENT_X = 27;

	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);

	public ItemInsideRecipeCategory(
			RvCategoryType<ItemInsideRecipe> type,
			ResourceLocation id,
			RVHelper rvHandler
	) {
		super(
				type,
				id,
				rvHandler,
				INPUT_BLOCK_POSITION,
				METHOD_POSITION,
				INPUT_INGREDIENT_X);
	}

	@Override
	public void configureDecorations(
			RvCategoryWidgetBuilder builder,
			RecipeHolder<ItemInsideRecipe> recipeHolder,
			Vector2fc position
	) {
		super.configureDecorations(builder, recipeHolder, position);
		var recipe = recipeHolder.value();
		if (recipe.time() > 0) {
			builder.addElement(new TextElementRenderer(ClientProxy.format("tip.lychee.sec", recipe.time()))
					.color(0xFFFFFFFF)
					.centered()
					.shadow()
					.at(position)
					.offset(methodPosition)
					.offset(10, -6));
		}
	}
}
