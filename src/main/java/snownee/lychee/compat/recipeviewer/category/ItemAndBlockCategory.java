package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import com.google.common.base.Suppliers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.ui.SpriteElementRenderer;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ItemAndBlockCategory<R extends ILycheeRecipe<LycheeContext>> extends AbstractRvCategory<R> {
	public static final Vector2fc INPUT_BLOCK_POSITION = new Vector2f(30, 35);
	public static final Vector2fc METHOD_POSITION = new Vector2f(26, 12);
	public static final float INPUT_INGREDIENT_X = 12;

	public static final int INPUT_BLOCK_SIZE = 18;
	public static final int METHOD_SIZE = 20;

	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);

	protected final Vector2fc inputBlockPosition;
	protected final Vector2fc methodPosition;
	protected final float inputIngredientX;

	protected ItemAndBlockCategory(
			RvCategoryType<R> type,
			ResourceLocation id,
			RVHelper rvHandler,
			Vector2fc inputBlockPosition,
			Vector2fc methodPosition,
			float inputIngredientX
	) {
		super(type, id, rvHandler);
		this.inputBlockPosition = inputBlockPosition;
		this.methodPosition = methodPosition;
		this.inputIngredientX = inputIngredientX;
	}

	public ItemAndBlockCategory(RvCategoryType<R> type, ResourceLocation id, RVHelper rvHandler) {
		this(type, id, rvHandler, INPUT_BLOCK_POSITION, METHOD_POSITION, INPUT_INGREDIENT_X);
	}

	protected BlockState getRenderingBlock(R recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(((BlockKeyableRecipe) recipe).blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				1000);
	}

	protected boolean shouldRenderInputBlockTooltip(R recipe) {
		return !BlockPredicateExtensions.isAny(((BlockKeyableRecipe) recipe).blockPredicate());
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();
		var needSecondLine = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9;
		var y = needSecondLine ? 26 : 28;
		builder.ingredientGroup(recipe, new Vector2f(inputIngredientX, y));
		builder.actionGroup(recipe, new Vector2f(width() - 29, y));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		builder.addElement(getInputBlockElement(recipe).offset(position));

		var methodElement = geMethodElement();
		if (methodElement != RenderElement.EMPTY) {
			builder.addElement(methodElement.offset(position));
		}

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = VectorExtensions.offset(
					inputBlockPosition,
					position.x() + INPUT_BLOCK_SIZE - 4,
					position.y() + INPUT_BLOCK_SIZE - 8);
			AbstractRvCategory.getRemoveInputIcon().at(removeActionPosition);
		}
	}

	protected RenderElement geMethodElement() {
		return RenderElement.create(AllGuiTextures.DOWN_ARROW).at(methodPosition).withSize(METHOD_SIZE);
	}

	protected RenderElement getInputBlockElement(R recipe) {
		var questionMarkElement = Suppliers.<RenderElement>memoize(() ->
				RenderElement.create(AllGuiTextures.QUESTION_MARK).at(4, 2));

		var result = getBlockElementWithShadow(() -> getRenderingBlock(recipe), questionMarkElement);

		if (shouldRenderInputBlockTooltip(recipe)) {
			result.onTooltip(() -> BlockPredicateExtensions.getTooltips(
					getRenderingBlock(recipe),
					((BlockKeyableRecipe) recipe).blockPredicate()));
		}

		return result.onClick(button ->
						rvHelper().buttonToUsageOrRecipe(button)
								.ifPresent(usageOrRecipe -> rvHelper().openPage(getRenderingBlock(recipe), usageOrRecipe)))
				.at(inputBlockPosition)
				.withSize(INPUT_BLOCK_SIZE);
	}

	private @NotNull InteractiveRenderElement getBlockElementWithShadow(Supplier<BlockState> blockStateSupplier, final Supplier<RenderElement> questionMarkElement) {
		var shadowElement = getShadowElement();

		Function<BlockState, RenderElement> blockElement = (BlockState state) -> GuiGameElement.of(state)
				.rotateBlock(12.5, 202.5, 0)
				.scale(15)
				.lighting(RVs.BLOCK_LIGHTING)
				.withSize(INPUT_BLOCK_SIZE)
				.at(-1, 4);

		return new InteractiveRenderElement((element) -> {
			var state = blockStateSupplier.get();
			if (state.isAir()) {
				return questionMarkElement.get();
			}

			return RenderElement.create((graphics, ignored) -> {
				if (state.getLightEmission() < 5) {
					shadowElement.get().render(graphics);
				}
				blockElement.apply(state).render(graphics);
			});
		});
	}

	private @NotNull Supplier<RenderElement> getShadowElement() {
		var scale = 0.7F;
		var shadowPosition = new Vector2f(
				INPUT_BLOCK_SIZE / 2F - (52 /* Shadow sprite width */ * scale / 2) + 4F,
				INPUT_BLOCK_SIZE - 13 /* Shadow sprite height */ * scale - 1F);
		return Suppliers.memoize(() -> new SpriteElementRenderer(AllGuiTextures.SHADOW.id, 1F).withSize(36, 9).at(shadowPosition));
	}
}
