package snownee.lychee.compat.recipeviewer.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Vector2fc;
import org.joml.Vector2ic;

import com.google.common.base.Strings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
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
import snownee.lychee.util.ui.CategoryMetadata;
import snownee.lychee.util.ui.ElementRenderer;
import snownee.lychee.util.ui.UIElement;


public abstract class AbstractRvCategory<R extends ILycheeRecipe<LycheeContext>> implements RvCategory<R> {
	private final RvCategoryType<R> type;
	private final RvHelper rvHelper;
	private final ResourceLocation id;
	protected final RecipeHolder<CategoryMetadata> metadata;
	private final List<RecipeHolder<R>> recipes = new ArrayList<>();

	protected AbstractRvCategory(RvCategoryType<R> type, ResourceLocation id, RvHelper rvHelper) {
		this.type = type;
		this.id = id;
		this.rvHelper = rvHelper;
		this.metadata = rvHelper.getMetadata(this);
	}

	public static boolean needRemoveInputIcon(ILycheeRecipe<? extends LycheeContext> recipe) {
		return recipe.postActions().stream().anyMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.fancyDisplay());
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
		return InteractiveRenderElement.create(new SpriteElementRenderer(AllGuiTextures.INFO.id).<SpriteElementRenderer>withSize(
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
		return metadata.value().icon().map(ElementRenderer::of).orElseGet(RvCategory.super::icon);
	}

	@Override
	public List<Ingredient> workstations() {
		return metadata.value().workstation().orElseGet(RvCategory.super::workstations);
	}

	@Override
	public int width() {
		Vector2ic size = metadata.value().size().orElse(null);
		return size == null ? RvCategory.super.width() : size.x();
	}

	@Override
	public int height() {
		Vector2ic size = metadata.value().size().orElse(null);
		return size == null ? RvCategory.super.height() : size.y();
	}

	@Override
	public Vector2fc infoPosition() {
		return InfoElementHelper.INFO_POSITION;
	}

	@Override
	public boolean renderDefault() {
		return metadata.value().renderDefault();
	}

	@Override
	public void configureCustomDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		List<UIElement> extra = metadata.value().elements().orElse(Map.of()).getOrDefault("extra", List.of());
		for (UIElement element : extra) {
			builder.addElement(ElementRenderer.of(element).offset(position));
		}
	}
}
