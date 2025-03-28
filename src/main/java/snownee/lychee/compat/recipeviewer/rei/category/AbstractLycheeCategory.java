package snownee.lychee.compat.recipeviewer.rei.category;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.recipeviewer.RvCategory;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.elements.ScreenElementWidget;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class AbstractLycheeCategory<T extends ILycheeRecipe<LycheeContext>> implements DisplayCategory<LycheeDisplay<T>>, LycheeCategory<T> {
	private final CategoryIdentifier<? extends LycheeDisplay<T>> categoryIdentifier;
	private final RvCategory<T> rvCategory;
	public Renderer icon;

	public AbstractLycheeCategory(RvCategory<T> category) {
		this.rvCategory = category;
		this.categoryIdentifier = CategoryIdentifier.of(category.id);
		this.icon = new ScreenElementWidget(category.icon());
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
	public final int getDisplayWidth(LycheeDisplay<T> display) {
		return contentWidth();
	}

	@Override
	public final int contentWidth() {
		return LycheeCategory.super.contentWidth();
	}

	@Override
	public int getDisplayHeight() {
		return rvCategory.type.height;
	}

	@Override
	public Rect2i infoRect() {
		return rvCategory.type.infoRect;
	}

	@Override
	public RvCategory<T> rvCategory() {
		return rvCategory;
	}
}
