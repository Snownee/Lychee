package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.ui.TextElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.VectorExtensions;


public class ItemInsideRecipeCategory extends ItemAndBlockCategory<ItemInsideRecipe> {
	public static final Vector2fc INPUT_BLOCK_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 54);
	public static final Vector2fc METHOD_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.METHOD_POSITION, 54);
	private static final float INPUT_INGREDIENT_X = 27;

	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);

	public ItemInsideRecipeCategory(
			RvCategoryType<ItemInsideRecipe> type,
			ResourceLocation id,
			RvHelper rvHandler
	) {
		super(
				type,
				id,
				rvHandler);
	}

	@Override
	public void configureDefaultDecorations(
			RvCategoryWidgetBuilder builder,
			RecipeHolder<ItemInsideRecipe> recipeHolder,
			Vector2fc position
	) {
		super.configureDefaultDecorations(builder, recipeHolder, position);
		var recipe = recipeHolder.value();
		if (recipe.time() > 0) {
			builder.addElement(new TextElementRenderer(ClientProxy.format("tip.lychee.sec", recipe.time()))
					.centered()
					.at(position)
					.offset(methodPosition())
					.offset(10, -8));
		}
	}

	@Override
	public Vector2fc infoPosition() {
		return INFO_POSITION;
	}

	@Override
	public Vector2fc inputBlockPosition() {
		return INPUT_BLOCK_POSITION;
	}

	@Override
	public Vector2fc methodPosition() {
		return METHOD_POSITION;
	}

	@Override
	public float inputIngredientX() {
		return INPUT_INGREDIENT_X;
	}
}
