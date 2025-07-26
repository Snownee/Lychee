package snownee.lychee.compat.recipeviewer.emi.category;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.emi.element.EmiRenderableAdapter;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategoryAdapter<R extends ILycheeRecipe<LycheeContext>> extends EmiRecipeCategory {
	private final RvCategoryInstance<R> instance;

	public RvCategoryAdapter(RvCategoryInstance<R> instance) {
		super(instance.id(), new EmiRenderableAdapter(instance.icon()));
		this.instance = instance;
	}

	@Override
	public Component getName() {
		return instance.title();
	}

	@Override
	public void render(GuiGraphics draw, int x, int y, float delta) {
		super.render(draw, x, y, delta);
	}

	public RvCategoryInstance<R> instance() {
		return instance;
	}
}
