package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ItemShapelessRecipeCategory<R extends ILycheeRecipe<LycheeContext>> extends AbstractRvCategory<R> {
	public static final int ICON_SIZE = 24;
	public static final Vector2fc ICON_POSITION = new Vector2f((float) RvCategoryType.WIDER_WIDTH / 2 - 8, 19);
	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(
			ICON_POSITION,
			(float) InfoElementHelper.INFO_SIZE / 2,
			-InfoElementHelper.INFO_SIZE);

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
		builder.ingredientGroup(recipe, new Vector2f(27, 28));
		builder.actionGroup(recipe, new Vector2f(width() - 29, 28));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		if (needRenderIcon()) {
			builder.addElement(RenderElement.create((graphics, element) -> {
				var stack = graphics.pose();
				stack.pushPose();
				stack.translate(0, 0, 100);
				icon().render(graphics);
				stack.popPose();
			}).at(ICON_POSITION).offset(position).withSize(ICON_SIZE));
		}
	}

	protected boolean needRenderIcon() {
		return true;
	}

	@Override
	public Vector2fc infoPosition() {
		return INFO_POSITION;
	}
}
