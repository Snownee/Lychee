package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
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
	private static final Vector2fc MOUSE_ICON_POSITION = new Vector2f(51, 15);
	private static final float INPUT_INGREDIENT_X = 22;
	private static final Vector2fc INPUT_BLOCK_POSITION = VectorExtensions.offset(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 18, 0);
	private static final Vector2fc METHOD_POSITION = VectorExtensions.offset(ItemAndBlockCategory.METHOD_POSITION, 18, 0);

	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);

	public BlockInteractingRecipeCategory(
			RvCategoryType<BlockInteractingRecipe> type,
			ResourceLocation id,
			RVHelper rvHandler
	) {
		super(type, id, rvHandler, INPUT_BLOCK_POSITION, METHOD_POSITION, INPUT_INGREDIENT_X);
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
		builder.addElement(getMouseIcon(recipeHolder.value()));
	}
}
