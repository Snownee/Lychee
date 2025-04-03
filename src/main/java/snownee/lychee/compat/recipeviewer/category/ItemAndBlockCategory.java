package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Function;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.element.ShadowElement;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;

@NotNullByDefault
public class ItemAndBlockCategory<R extends ILycheeRecipe<LycheeContext>> extends AbstractRvCategory<R> {
	public static final Vector2fc INPUT_BLOCK_POSITION = new Vector2f(22, 32);
	public static final Vector2fc METHOD_POSITION = new Vector2f(INPUT_BLOCK_POSITION.x() - 4, 10);
	public static final float INPUT_INGREDIENT_X = 12;
	public static final int BLOCK_SIZE = 18;
	public static final int METHOD_SIZE = 20;
	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);
	private final ShadowElement shadowElement = new ShadowElement(BLOCK_SIZE, 36, 9);

	protected ItemAndBlockCategory(
			RvCategoryType<R> type,
			ResourceLocation id,
			RvHelper rvHandler
	) {
		super(type, id, rvHandler);
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
	public Vector2fc infoPosition() {
		return INFO_POSITION;
	}

	public Vector2fc inputBlockPosition() {
		return INPUT_BLOCK_POSITION;
	}

	public Vector2fc methodPosition() {
		return METHOD_POSITION;
	}

	public float inputIngredientX() {
		return INPUT_INGREDIENT_X;
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();
		builder.ingredientGroup(recipe, new Vector2f(inputIngredientX(), 28));
		builder.actionGroup(recipe, new Vector2f(width() - 29, 28));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		builder.addElement(getInputBlockElement(recipe).offset(position));

		var methodElement = getMethodElement(recipeHolder.value());
		if (methodElement != RenderElement.EMPTY) {
			builder.addElement(methodElement.offset(position));
		}

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = VectorExtensions.offset(
					inputBlockPosition(),
					BLOCK_SIZE - 4,
					BLOCK_SIZE - 8);
			builder.addElement(AbstractRvCategory.getRemoveInputIcon().at(removeActionPosition).offset(position));
		}
	}

	protected RenderElement getMethodElement(R recipe) {
		return RenderElement.create(AllGuiTextures.DOWN_ARROW).at(methodPosition()).withSize(METHOD_SIZE);
	}

	protected RenderElement getInputBlockElement(R recipe) {
		Function<BlockState, RenderElement> blockElement = blockState -> GuiGameElement.of(blockState)
				.rotateBlock(12.5, 160, 0)
				.scale(15)
				.lighting(RVs.BLOCK_LIGHTING)
				.withSize(BLOCK_SIZE)
				.at(-1, 4);
		var result = shadowElement.blockWithShadow(() -> getRenderingBlock(recipe), blockElement);

		if (shouldRenderInputBlockTooltip(recipe)) {
			result.onTooltip(() -> BlockPredicateExtensions.getTooltips(
					getRenderingBlock(recipe),
					((BlockKeyableRecipe) recipe).blockPredicate()));
		}

		return result.onClick(button ->
						rvHelper().buttonToUsageOrRecipe(button)
								.ifPresent(usageOrRecipe -> rvHelper().openPage(getRenderingBlock(recipe), usageOrRecipe)))
				.at(inputBlockPosition())
				.withSize(BLOCK_SIZE);
	}
}
