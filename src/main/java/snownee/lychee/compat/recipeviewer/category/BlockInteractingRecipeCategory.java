package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.util.VectorExtensions;

public class BlockInteractingRecipeCategory extends ItemAndBlockCategory<BlockInteractingRecipe> {
	private static final float INPUT_INGREDIENT_X = 22;

	public static final Vector2fc INPUT_BLOCK_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 46);
	public static final Vector2fc METHOD_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.METHOD_POSITION, 46);
	public static final int MOUSE_ICON_SIZE = 16;
	public static final Vector2fc MOUSE_ICON_POSITION = VectorExtensions.offset(METHOD_POSITION, -MOUSE_ICON_SIZE + 2, 0);

	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);

	public BlockInteractingRecipeCategory(
			RvCategoryType<BlockInteractingRecipe> type,
			ResourceLocation id,
			RVHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

	private RenderElement getMouseIcon(BlockInteractingRecipe recipe) {
		var icon = recipe.getType() == RecipeTypes.BLOCK_CLICKING ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		return RenderElement.create(icon).at(MOUSE_ICON_POSITION);
	}

	@Override
	public void configureDecorations(
			RvCategoryWidgetBuilder builder,
			RecipeHolder<BlockInteractingRecipe> recipeHolder,
			Vector2fc position
	) {
		super.configureDecorations(builder, recipeHolder, position);
		builder.addElement(getMouseIcon(recipeHolder.value()).offset(position));
	}

	@Override
	public Vector2fc infoPosition() {
		return INFO_POSITION;
	}

	public Vector2fc inputBlockPosition() {
		return INPUT_BLOCK_POSITION;
	}

	public Vector2fc methodPosition() {
		return METHOD_POSITION;
	}

	public float inputIngredientX() {
		return INPUT_INGREDIENT_X;
	}
}
