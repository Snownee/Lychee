package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Function;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
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

public class ItemAndBlockCategory<R extends ILycheeRecipe<LycheeContext>> extends RvCategory<R> {
	public static final Vector2fc INPUT_BLOCK_POSITION = new Vector2f(22, 32);
	public static final Vector2fc METHOD_POSITION = new Vector2f(INPUT_BLOCK_POSITION.x() - 4, 10);
	public static final float INPUT_INGREDIENT_X = 12;
	public static final int BLOCK_SIZE = 18;
	public static final int METHOD_SIZE = 20;
	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);
	private static final ShadowElement SHADOW_ELEMENT = new ShadowElement(BLOCK_SIZE, 36, 9);

	public Vector2fc inputBlockPosition() {
		return INPUT_BLOCK_POSITION;
	}

	@Override
	public Vector2fc infoPosition(R recipe) {
		return INFO_POSITION;
	}

	public Vector2fc methodPosition() {
		return METHOD_POSITION;
	}

	public float inputIngredientX() {
		return INPUT_INGREDIENT_X;
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder<R> builder, RecipeHolder<R> recipeHolder) {
		var recipe = recipeHolder.value();
		builder.ingredientGroup(recipe, new Vector2f(inputIngredientX(), 28));
		builder.actionGroup(recipe, new Vector2f(builder.width() - 29, 28));
	}

	@Override
	public void setupDecorations(DecorationMapBuilder<R> mapBuilder) {
		mapBuilder.info(this::infoPosition);
		mapBuilder.consumeBlockInput($ -> VectorExtensions.offset(inputBlockPosition(), BLOCK_SIZE - 4, BLOCK_SIZE - 8));

		mapBuilder.put(
				"block_in", (builder, recipeHolder) -> {
					R recipe = recipeHolder.value();
					InteractiveRenderElement element = getInputBlockElement(recipe, inputBlockPosition(), builder.helper());
					if (shouldRenderInputBlockTooltip(recipe)) {
						element.onTooltip(() -> BlockPredicateExtensions.getTooltips(
								getRenderingBlock(recipe),
								((BlockKeyableRecipe) recipe).blockPredicate()));
					}
					builder.addElement(element);
				});

		mapBuilder.put(
				"method",
				(builder, recipeHolder) -> builder.addElement(
						RenderElement.create(AllGuiTextures.DOWN_ARROW).at(methodPosition()).withSize(METHOD_SIZE)));
	}

	protected BlockState getRenderingBlock(R recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(((BlockKeyableRecipe) recipe).blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				1000);
	}

	protected boolean shouldRenderInputBlockTooltip(R recipe) {
		return true;
	}

	protected InteractiveRenderElement getInputBlockElement(
			R recipe,
			Vector2fc inputBlockPosition,
			RvHelper helper) {
		Function<BlockState, RenderElement> blockElement = blockState -> GuiGameElement.of(blockState)
				.rotateBlock(12.5, 160, 0)
				.scale(15)
				.lighting(RVs.BLOCK_LIGHTING)
				.withSize(BLOCK_SIZE)
				.at(-1, 4);
		var result = SHADOW_ELEMENT.blockWithShadow(() -> getRenderingBlock(recipe), blockElement);

		if (shouldRenderInputBlockTooltip(recipe)) {
			result.onTooltip(() -> BlockPredicateExtensions.getTooltips(
					getRenderingBlock(recipe),
					((BlockKeyableRecipe) recipe).blockPredicate()));
		}

		return result.onClick(helper.lookupBlock(() -> getRenderingBlock(recipe)))
				.at(inputBlockPosition)
				.withSize(BLOCK_SIZE);
	}
}
