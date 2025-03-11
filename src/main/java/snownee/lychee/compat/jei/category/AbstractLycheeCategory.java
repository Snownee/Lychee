package snownee.lychee.compat.jei.category;

import java.util.List;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.Lychee;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.category.SpriteElement;
import snownee.lychee.category.SpriteElementRenderer;
import snownee.lychee.compat.jei.elements.InteractiveWidget;
import snownee.lychee.compat.jei.elements.ScreenElementWidget;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

@NotNullByDefault
public abstract class AbstractLycheeCategory<T extends ILycheeRecipe<LycheeContext>> implements IRecipeCategory<RecipeHolder<T>>, LycheeCategory<T> {
	protected Rect2i infoRect = new Rect2i(4, 25, 8, 8);
	public static final int WIDTH = 119;
	public static final int HEIGHT = 59;

	private static final ResourceLocation REMOVE_BLOCK_SPRITE = Lychee.id("rv/remove_block");

	private final RecipeType<RecipeHolder<T>> type;
	private final RvCategory<T> rvCategory;
	public IDrawable icon;

	public AbstractLycheeCategory(RecipeType<RecipeHolder<T>> type, RvCategory<T> category) {
		this.type = type;
		this.rvCategory = category;
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

	protected static <T extends ILycheeRecipe<LycheeContext>> void addRemoveInputBlock(
			int x,
			int y,
			IRecipeExtrasBuilder builder,
			RecipeHolder<T> recipeHolder) {
		if (recipeHolder.value().postActions().stream().noneMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.hidden())) {
			return;
		}
		var widget = new InteractiveWidget(new ScreenRectangle(x, y, 8, 8), true);
		builder.addWidget(widget);
		builder.addGuiEventListener(widget);
		widget.setRenderable(new SpriteElementRenderer(
				new SpriteElement(Lychee.id("rv/remove_block")),
				widget.getPosition().x(),
				widget.getPosition().y(),
				100,
				widget.getWidth(),
				widget.getHeight()));
		widget.setTooltipFunction(it -> List.of(Component.translatable("postAction.lychee.place.consume")));
	}
}
