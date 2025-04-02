package snownee.lychee.compat.recipeviewer.category;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.joml.Vector2fc;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.Lychee;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.ui.SpriteElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

@NotNullByDefault
public abstract class AbstractRvCategory<R extends ILycheeRecipe<LycheeContext>> implements RvCategory<R> {
	private final RvCategoryType<R> type;
	private final RvHelper rvHelper;

	private final Supplier<RenderElement> iconSupplier;
	private final ResourceLocation id;

	private final List<RecipeHolder<R>> recipes = new ArrayList<>();

	protected AbstractRvCategory(RvCategoryType<R> type, ResourceLocation id, RvHelper rvHelper) {
		this.type = type;
		this.id = id;
		this.iconSupplier = Suppliers.memoize(() -> type.iconProvider.get(this));
		this.rvHelper = rvHelper;
	}

	public static boolean needRemoveInputIcon(ILycheeRecipe<? extends LycheeContext> recipe) {
		return recipe.postActions().stream()
				.anyMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.hidden());
	}

	public static RenderElement getRemoveInputIcon() {
		return new InteractiveRenderElement((InteractiveRenderElement element) ->
				new SpriteElementRenderer(Lychee.id("exclamation_mark"), 2)
						.withSize(element.width(), element.height()).atZ(100)
		).onTooltip(() -> List.of(Component.translatable("postAction.lychee.place.consume")))
				.withSize(InfoElementHelper.INFO_SIZE, InfoElementHelper.INFO_SIZE);
	}

	public static boolean needInfoIcon(ILycheeRecipe<?> recipe) {
		return !recipe.conditions().conditions().isEmpty() || recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false);
	}

	public static <R extends ILycheeRecipe<?>> RenderElement getRecipeInfoIcon(RecipeHolder<R> recipeHolder) {
		var recipe = recipeHolder.value();
		return new InteractiveRenderElement(new SpriteElementRenderer(AllGuiTextures.INFO.id, 1).<SpriteElementRenderer>withSize(
				InfoElementHelper.INFO_SIZE))
				.onTooltip(() -> RVs.getRecipeTooltip(recipe))
				.onClick((button) -> ClientProxy.postInfoBadgeClickEvent(recipe, recipeHolder.id(), button))
				.withSize(InfoElementHelper.INFO_SIZE);
	}

	public RenderElement getInfoIcon(RecipeHolder<R> recipeHolder) {
		return getRecipeInfoIcon(recipeHolder).at(infoPosition());
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
	public RvHelper rvHelper() {
		return rvHelper;
	}

	@Override
	public List<RecipeHolder<R>> recipes() {
		return recipes;
	}

	@Override
	public RenderElement icon() {
		return iconSupplier.get();
	}

	@Override
	public Vector2fc infoPosition() {
		return InfoElementHelper.INFO_POSITION;
	}
}
