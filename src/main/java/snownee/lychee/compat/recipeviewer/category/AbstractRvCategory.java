package snownee.lychee.compat.recipeviewer.category;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.joml.Vector2ic;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.Lychee;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.ui.SpriteElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class AbstractRvCategory<R extends ILycheeRecipe<LycheeContext>> implements RvCategory<R> {
	private final RvCategoryType<R> type;
	private final RVCategoryHandler rvHandler;

	private final Supplier<RenderElement> iconSupplier;
	private final ResourceLocation id;

	private final List<RecipeHolder<R>> recipes = new ArrayList<>();

	protected AbstractRvCategory(RvCategoryType<R> type, ResourceLocation id, RVCategoryHandler rvHandler) {
		this.type = type;
		this.id = id;
		this.iconSupplier = Suppliers.memoize(() -> type.iconProvider.get(this));
		this.rvHandler = rvHandler;
	}

	public static boolean needRemoveInputIcon(ILycheeRecipe<? extends LycheeContext> recipe) {
		return recipe.postActions().stream()
				.noneMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.hidden());
	}

	public static RenderElement getRemoveInputIcon() {
		return new InteractiveRenderElement(element ->
				new SpriteElementRenderer(
						Lychee.id("exclamation_mark"),
						new Rect2i(element.x(), element.y(), element.width(), element.height()),
						100,
						2)
		).withSize(InfoElementHelper.INFO_SIZE, InfoElementHelper.INFO_SIZE);
	}

	public static boolean needInfoIcon(ILycheeRecipe<?> recipe) {
		return recipe.conditions().conditions().isEmpty() && !recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false);
	}

	public static <R extends ILycheeRecipe<LycheeContext>> RenderElement getInfoIcon(RecipeHolder<R> recipeHolder, Vector2ic infoPosition) {
		var recipe = recipeHolder.value();
		return new InteractiveRenderElement(AllGuiTextures.INFO)
				.onTooltip(() -> RVs.getRecipeTooltip(recipe))
				.onClick((button) -> ClientProxy.postInfoBadgeClickEvent(recipe, recipeHolder.id(), button))
				.withSize(InfoElementHelper.INFO_SIZE, InfoElementHelper.INFO_SIZE)
				.at(infoPosition);
	}

	public RenderElement getInfoIcon(RecipeHolder<R> recipeHolder) {
		return getInfoIcon(recipeHolder, infoPosition());
	}

	@Override
	public RvCategoryType<R> type() {
		return type;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public RVCategoryHandler rvHandler() {
		return rvHandler;
	}

	@Override
	public List<RecipeHolder<R>> recipes() {
		return recipes;
	}

	@Override
	public RenderElement icon() {
		return iconSupplier.get();
	}
}
