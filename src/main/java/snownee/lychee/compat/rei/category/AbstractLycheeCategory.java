package snownee.lychee.compat.rei.category;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class AbstractLycheeCategory<T extends ILycheeRecipe<LycheeContext>> extends LycheeDisplayCategory<LycheeDisplay<T>> implements LycheeCategory<T> {
	protected Rect2i infoRect = new Rect2i(4, 25, 8, 8);

	public AbstractLycheeCategory(
			CategoryIdentifier<? extends LycheeDisplay<T>> categoryIdentifier,
			Renderer icon
	) {
		super(categoryIdentifier, icon);
	}

	@Override
	public Rect2i infoRect() {
		return infoRect;
	}
}
