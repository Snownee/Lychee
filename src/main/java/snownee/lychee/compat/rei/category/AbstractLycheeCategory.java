package snownee.lychee.compat.rei.category;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.ScreenElementWidget;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class AbstractLycheeCategory<T extends ILycheeRecipe<LycheeContext>> implements DisplayCategory<LycheeDisplay<T>>, LycheeCategory<T> {
	protected Rect2i infoRect = new Rect2i(4, 25, 8, 8);
	public static final int WIDTH = 150;
	public static final int HEIGHT = 59;

	private final CategoryIdentifier<? extends LycheeDisplay<T>> categoryIdentifier;
	private final RvCategory<T> rvCategory;
	public Renderer icon;

	public AbstractLycheeCategory(CategoryIdentifier<? extends LycheeDisplay<T>> id, RvCategory<T> category) {
		this.categoryIdentifier = id;
		this.rvCategory = category;
		icon = new ScreenElementWidget(category.icon());
	}

	@Override
	public Renderer getIcon() {
		return icon;
	}

	@Override
	public Component getTitle() {
		return rvCategory.title();
	}

	@Override
	public CategoryIdentifier<? extends LycheeDisplay<T>> getCategoryIdentifier() {
		return categoryIdentifier;
	}

	@Override
	public int getDisplayHeight() {
		return HEIGHT + 8;
	}

	@Override
	public Rect2i infoRect() {
		return infoRect;
	}

	@Override
	public RvCategory<T> rvCategory() {
		return rvCategory;
	}
}
