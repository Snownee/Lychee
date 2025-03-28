package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ItemShapelessRecipeCategory<R extends ILycheeRecipe<LycheeContext>> extends AbstractRvCategory<R> {
	public static final Vector2ic INFO_POSITION = new Vector2i(3, 25);

	protected ItemShapelessRecipeCategory(
			RvCategoryType<R> type,
			ResourceLocation id,
			RVHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<R> recipeHolder, Vector2ic position) {
		var recipe = recipeHolder.value();
		var centerX = position.x() + width() / 2;
		var needSecondLine = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9;
		var y = position.y() + (needSecondLine ? 26 : 28);
		builder.ingredientGroup(recipe, new Vector2i(centerX - 45, y));
		builder.actionGroup(recipe, new Vector2i(centerX + 50, y));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2ic position) {
		var centerX = position.x() + width() / 2;
		var recipe = recipeHolder.value();

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		builder.addElement(RenderElement.create((graphics, x, y) -> {
			var stack = graphics.pose();
			stack.pushPose();
			stack.translate(x, y, 100);
			icon().render(graphics);
			stack.popPose();
		}).at(centerX - 8, position.y() + 19).withSize(24, 24));
	}
}
