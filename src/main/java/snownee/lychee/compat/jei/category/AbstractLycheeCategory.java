package snownee.lychee.compat.jei.category;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.jei.elements.DrawableSimpleItemStack;
import snownee.lychee.compat.jei.elements.InteractiveWidget;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

@NotNullByDefault
public abstract class AbstractLycheeCategory<T extends ILycheeRecipe<LycheeContext>> implements IRecipeCategory<RecipeHolder<T>>, LycheeCategory<T> {
	protected Rect2i infoRect = new Rect2i(4, 25, 8, 8);
	public static final int WIDTH = 119;
	public static final int HEIGHT = 59;

	private final RecipeType<RecipeHolder<T>> type;
	private RvCategory<T> rvCategory;
	public IDrawable icon;

	public AbstractLycheeCategory(RecipeType<RecipeHolder<T>> type, IDrawable icon) {
		this.icon = icon;
		this.type = type;
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
		return RVs.makeTitle(getRecipeType().getUid());
	}

	@Override
	public Rect2i infoRect() {
		return infoRect;
	}

	@Override
	public int getWidth() {
		return contentWidth();
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		createInfoBadgeIfNeeded(builder, recipeHolder);
	}

	@Override
	public RvCategory<T> rvCategory() {
		return rvCategory;
	}

	protected static <T extends ILycheeRecipe<LycheeContext>> void addRemoveInput(
			int x,
			int y,
			IRecipeExtrasBuilder builder,
			RecipeHolder<T> recipeHolder) {
		if (recipeHolder.value().postActions().stream().noneMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.hidden())) {
			return;
		}
		builder.addDrawable(new DrawableSimpleItemStack(Items.BARRIER.getDefaultInstance(), x, y, 0.5F));
	}
}
