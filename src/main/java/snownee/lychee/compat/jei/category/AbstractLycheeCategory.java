package snownee.lychee.compat.jei.category;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.compat.jei.elements.ScreenElementWidget;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

@NotNullByDefault
public abstract class AbstractLycheeCategory<T extends ILycheeRecipe<LycheeContext>> implements IRecipeCategory<RecipeHolder<T>>, LycheeCategory<T> {
	private final RecipeType<RecipeHolder<T>> type;
	private final RvCategory<T> rvCategory;
	public IDrawable icon;

	public AbstractLycheeCategory(RvCategory<T> category) {
		this.rvCategory = category;
		this.type = RecipeType.createRecipeHolderType(category.id);
		icon = new ScreenElementWidget(category.icon());
	}

	@Override
	public RecipeType<RecipeHolder<T>> getRecipeType() {
		return type;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public Component getTitle() {
		return rvCategory.title();
	}

	@Override
	public Rect2i infoRect() {
		return rvCategory.type.infoRect;
	}

	@Override
	public int getWidth() {
		return contentWidth();
	}

	@Override
	public int getHeight() {
		return rvCategory.type.height;
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		createInfoBadgeIfNeeded(builder, recipeHolder);
	}

	@Override
	public RvCategory<T> rvCategory() {
		return rvCategory;
	}
}
