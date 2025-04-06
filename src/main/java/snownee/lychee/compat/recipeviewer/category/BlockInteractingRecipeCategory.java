package snownee.lychee.compat.recipeviewer.category;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.util.VectorExtensions;


public class BlockInteractingRecipeCategory extends ItemAndBlockCategory<BlockInteractingRecipe> {
	private static final float INPUT_INGREDIENT_X = 22;

	public static final Vector2fc INPUT_BLOCK_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 34);
	public static final Vector2fc METHOD_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.METHOD_POSITION, 40);

	public static final Vector2fc INFO_POSITION = new Vector2f(INPUT_INGREDIENT_X - 4, 40);

	public BlockInteractingRecipeCategory(
			RvCategoryType<BlockInteractingRecipe> type,
			ResourceLocation id,
			RvHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

	@Override
	protected RenderElement getMethodElement(BlockInteractingRecipe recipe) {
		var icon = recipe.getType() == RecipeTypes.BLOCK_CLICKING ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		return InteractiveRenderElement.create(icon)
				.onTooltip(() -> List.of(Component.translatable(Util.makeDescriptionId(
						"tip",
						BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer())))))
				.withSize(16)
				.at(methodPosition());
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
