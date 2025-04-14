package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Function;
import java.util.function.Supplier;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.Direction;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.element.ShadowElement;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class DripstoneRecipeCategory extends RvCategory<DripstoneRecipe> {
	private static final int BLOCK_SIZE = 12;
	private static final int COLUMN_X = 16;
	private static final Vector2fc SOURCE_BLOCK_POSITION = new Vector2f(COLUMN_X, 4);
	private static final Vector2fc DRIPSTONE_POSITION = VectorExtensions.offsetY(SOURCE_BLOCK_POSITION, BLOCK_SIZE);
	private static final Vector2fc POINTED_DRIPSTONE_POSITION = VectorExtensions.offsetY(SOURCE_BLOCK_POSITION, BLOCK_SIZE * 2);
	public static final Vector2fc INFO_POSITION = VectorExtensions.offsetX(POINTED_DRIPSTONE_POSITION, BLOCK_SIZE);
	private static final Vector2fc TARGET_BLOCK_POSITION = VectorExtensions.offsetY(SOURCE_BLOCK_POSITION, BLOCK_SIZE * 3);
	private final ShadowElement shadowElement = new ShadowElement(BLOCK_SIZE, 24, 8);

	private BlockState getSourceBlock(DripstoneRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.sourceBlock()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}

	private BlockState getTargetBlock(DripstoneRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}

	@Override
	public Vector2fc infoPosition(DripstoneRecipe recipe) {
		return INFO_POSITION;
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder<DripstoneRecipe> builder, RecipeHolder<DripstoneRecipe> recipeHolder) {
		var recipe = recipeHolder.value();
		var needSecondLine = recipe.conditions().showingCount() > 9;
		var y = (needSecondLine ? 26 : 28);
		builder.actionGroup(recipe, new Vector2f(builder.width() - 29, y));
	}

	@Override
	public void setupDecorations(DecorationMapBuilder<DripstoneRecipe> mapBuilder) {
		mapBuilder.info(this::infoPosition);
		mapBuilder.consumeBlockInput($ -> VectorExtensions.offset(TARGET_BLOCK_POSITION, BLOCK_SIZE - 4, BLOCK_SIZE - 8));

		mapBuilder.put(
				"source_block", (builder, recipeHolder) -> {
					var recipe = recipeHolder.value();
					builder.addElement(getBlockElement(() -> getSourceBlock(recipe), recipe.sourceBlock(), builder.helper())
							.at(SOURCE_BLOCK_POSITION)
							.withSize(BLOCK_SIZE));
				});

		mapBuilder.put(
				"dripstone_block", (builder, recipeHolder) -> {
					builder.addElement(
							getBlockElement(Blocks.DRIPSTONE_BLOCK::defaultBlockState, BlockPredicateExtensions.ANY, builder.helper())
									.at(DRIPSTONE_POSITION)
									.withSize(BLOCK_SIZE));
					BlockState blockState = Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(
							PointedDripstoneBlock.TIP_DIRECTION,
							Direction.DOWN);
					builder.addElement(
							getBlockElement(() -> blockState, BlockPredicateExtensions.ANY, builder.helper())
									.at(POINTED_DRIPSTONE_POSITION)
									.withSize(BLOCK_SIZE));
				});

		mapBuilder.put(
				"target_block",
				(builder, recipeHolder) -> builder.addElement(
						getTargetBlockElement(recipeHolder.value(), builder.helper())));
	}

	protected RenderElement getBlockElement(Supplier<BlockState> stateSupplier, BlockPredicate predicate, RvHelper helper) {
		Supplier<RenderElement> blockElement = () -> GuiGameElement.of(stateSupplier.get())
				.scale(BLOCK_SIZE)
				.lighting(RVs.BLOCK_LIGHTING)
				.rotateBlock(12.5, -22.5, 0);
		return new InteractiveRenderElement((InteractiveRenderElement element) -> blockElement.get())
				.onTooltip(() -> BlockPredicateExtensions.getTooltips(stateSupplier.get(), predicate, helper))
				.onInput(helper.inputOnBlock(stateSupplier))
				.withSize(BLOCK_SIZE);
	}

	protected RenderElement getTargetBlockElement(DripstoneRecipe recipe, RvHelper helper) {
		Function<BlockState, RenderElement> blockElement = blockState -> GuiGameElement.of(blockState)
				.rotateBlock(12.5, -22.5, 0)
				.scale(BLOCK_SIZE)
				.lighting(RVs.BLOCK_LIGHTING)
				.withSize(BLOCK_SIZE);
		var result = shadowElement.blockWithShadow(() -> getTargetBlock(recipe), blockElement);

		return result
				.onTooltip(() -> BlockPredicateExtensions.getTooltips(getTargetBlock(recipe), recipe.blockPredicate(), helper))
				.onInput(helper.inputOnBlock(() -> getTargetBlock(recipe)))
				.at(TARGET_BLOCK_POSITION)
				.withSize(BLOCK_SIZE);
	}
}
