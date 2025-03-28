package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.util.VectorExtensions;

public class BlockInteractingRecipeCategory extends ItemAndBlockCategory<BlockInteractingRecipe> {
	private static final Vector2ic MOUSE_ICON_POSITION = new Vector2i(51, 15);
	private static final Vector2ic INGREDIENT_POSITION = new Vector2i(22, 21);
	private static final Vector2ic INPUT_BLOCK_POSITION = VectorExtensions.offset(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 18, 0);
	private static final Vector2ic METHOD_POSITION = VectorExtensions.offset(ItemAndBlockCategory.METHOD_POSITION, 18, 0);

	public BlockInteractingRecipeCategory(
			RvCategoryType<BlockInteractingRecipe> type,
			ResourceLocation id,
			RVCategoryHandler rvHandler
	) {
		super(type, id, rvHandler, INPUT_BLOCK_POSITION, METHOD_POSITION, INGREDIENT_POSITION);
	}

	private RenderElement getMouseIcon(BlockInteractingRecipe recipe) {
		var icon = recipe.getType() == RecipeTypes.BLOCK_CLICKING ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		return RenderElement.create(icon).at(MOUSE_ICON_POSITION);
	}

	@Override
	public void configureDecorations(
			RvCategoryWidgetBuilder builder,
			RecipeHolder<BlockInteractingRecipe> recipeHolder,
			Vector2ic position
	) {
		super.configureDecorations(builder, recipeHolder, position);
		builder.addElement(getMouseIcon(recipeHolder.value()));
	}
}
