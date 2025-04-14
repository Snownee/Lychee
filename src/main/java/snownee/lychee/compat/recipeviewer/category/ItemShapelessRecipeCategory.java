package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ItemShapelessRecipeCategory<R extends ILycheeRecipe<LycheeContext>> extends RvCategory<R> {
	public static final int ICON_SIZE = 24;
	public static final Vector2fc ICON_POSITION = new Vector2f((float) RvCategory.WIDER_WIDTH / 2 - 8, 19);
	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(
			ICON_POSITION,
			(float) InfoElementHelper.INFO_SIZE / 2,
			-InfoElementHelper.INFO_SIZE);

	@Override
	public void setupDecorations(DecorationMapBuilder<R> mapBuilder) {
		mapBuilder.info(this::infoPosition);

		mapBuilder.put(
				"icon", (builder, recipeHolder) -> {
					RenderElement icon = builder.instance().icon();
					builder.addElement(RenderElement.create((graphics, element) -> {
						var stack = graphics.pose();
						stack.pushPose();
						stack.translate(0, 0, 100);
						icon.render(graphics);
						stack.popPose();
					}).at(ICON_POSITION).withSize(ICON_SIZE));
				});
	}

	@Override
	public Vector2fc infoPosition(R recipe) {
		return INFO_POSITION;
	}
}
