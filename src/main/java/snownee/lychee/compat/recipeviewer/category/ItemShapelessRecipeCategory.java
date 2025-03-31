package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ItemShapelessRecipeCategory<R extends ILycheeRecipe<LycheeContext>> extends AbstractRvCategory<R> {
	public static final Vector2fc INFO_POSITION = new Vector2f(3, 25);

	protected ItemShapelessRecipeCategory(
			RvCategoryType<R> type,
			ResourceLocation id,
			RVHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();
		var centerX = position.x() + (float) width() / 2;
		var needSecondLine = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9;
		var y = position.y() + (needSecondLine ? 26 : 28);
		builder.ingredientGroup(recipe, new Vector2f(centerX - 45, y));
		builder.actionGroup(recipe, new Vector2f(centerX + 50, y));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		var centerX = position.x() + (float) width() / 2;
		var recipe = recipeHolder.value();

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		builder.addElement(RenderElement.create((graphics, element) -> {
			var stack = graphics.pose();
			stack.pushPose();
			stack.translate(0, 0, 100);
			icon().render(graphics);
			stack.popPose();
		}).at(centerX - 8, position.y() + 19).offset(position).withSize(24, 24));
	}
}
