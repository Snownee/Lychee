package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.ui.TextElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.VectorExtensions;


public class ItemInsideRecipeCategory extends ItemAndBlockCategory<ItemInsideRecipe> {
	public static final Vector2fc INPUT_BLOCK_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 54);
	public static final Vector2fc METHOD_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.METHOD_POSITION, 54);
	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);
	private static final float INPUT_INGREDIENT_X = 27;

	@Override
	public void setupDecorations(DecorationMapBuilder<ItemInsideRecipe> mapBuilder) {
		super.setupDecorations(mapBuilder);
		mapBuilder.condition("time", $ -> $.time() > 0);
		mapBuilder.put(
				"time", (builder, recipeHolder) -> {
					builder.addElement(new TextElementRenderer(ClientProxy.format("tip.lychee.sec", recipeHolder.value().time())).centered()
							.offset(methodPosition())
							.offset(10, -8));
				});
	}

	@Override
	public Vector2fc infoPosition(ItemInsideRecipe recipe) {
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
