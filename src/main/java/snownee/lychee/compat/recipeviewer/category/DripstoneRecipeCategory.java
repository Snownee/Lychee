package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Function;
import java.util.function.Supplier;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
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

@NotNullByDefault
public class DripstoneRecipeCategory extends AbstractRvCategory<DripstoneRecipe> {
	private static final int BLOCK_SIZE = 16;
	private static final int COLUMN_X = 16;
	private static final Vector2fc SOURCE_BLOCK_POSITION = new Vector2f(COLUMN_X, 4);
	private static final Vector2fc DRIPSTONE_POSITION = VectorExtensions.offsetY(SOURCE_BLOCK_POSITION, 12);
	private static final Vector2fc POINTED_DRIPSTONE_POSITION = VectorExtensions.offsetY(SOURCE_BLOCK_POSITION, 12 * 2);
	public static final Vector2fc INFO_POSITION = VectorExtensions.offsetX(POINTED_DRIPSTONE_POSITION, BLOCK_SIZE);
	private static final Vector2fc TARGET_BLOCK_POSITION = VectorExtensions.offsetY(SOURCE_BLOCK_POSITION, 12 * 3);
	private final ShadowElement shadowElement = new ShadowElement(BLOCK_SIZE, 24, 6);

	protected DripstoneRecipeCategory(
			RvCategoryType<DripstoneRecipe> type,
			ResourceLocation id,
			RvHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

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
	public Vector2fc infoPosition() {
		return INFO_POSITION;
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<DripstoneRecipe> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();
		var needSecondLine = recipe.conditions().showingCount() > 9;
		var y = (needSecondLine ? 26 : 28);
		builder.actionGroup(recipe, new Vector2f(width() - 29, y));
	}

	@Override
	public void configureDefaultDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<DripstoneRecipe> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		builder.addElement(getBlockElement(recipe, () -> getSourceBlock(recipe)).at(SOURCE_BLOCK_POSITION).offset(position));
		builder.addElement(getBlockElement(recipe, Blocks.DRIPSTONE_BLOCK::defaultBlockState).at(DRIPSTONE_POSITION).offset(position));
		builder.addElement(getBlockElement(
				recipe,
				() -> Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
		).at(POINTED_DRIPSTONE_POSITION).offset(position));
		builder.addElement(getTargetBlockElement(recipe).offset(position));

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = VectorExtensions.offset(TARGET_BLOCK_POSITION, BLOCK_SIZE - 4, BLOCK_SIZE - 8);
			builder.addElement(AbstractRvCategory.getRemoveInputIcon().at(removeActionPosition).offset(position));
		}
	}

	protected RenderElement getBlockElement(
			DripstoneRecipe recipe,
			Supplier<BlockState> stateSupplier) {
		Supplier<RenderElement> blockElement = () -> GuiGameElement.of(stateSupplier.get())
				.scale(12)
				.lighting(RVs.BLOCK_LIGHTING)
				.rotateBlock(12.5, -22.5, 0)
				.at(-1, 2);
		return new InteractiveRenderElement((InteractiveRenderElement element) -> blockElement.get())
				.onTooltip(() -> BlockPredicateExtensions.getTooltips(stateSupplier.get(), recipe.blockPredicate()))
				.onClick(button -> rvHelper().buttonToUsageOrRecipe(button)
						.ifPresent(usageOrRecipe -> rvHelper().openPage(stateSupplier.get(), usageOrRecipe)))
				.withSize(BLOCK_SIZE);
	}

	protected RenderElement getTargetBlockElement(DripstoneRecipe recipe) {
		Function<BlockState, RenderElement> blockElement = blockState -> GuiGameElement.of(blockState)
				.rotateBlock(12.5, -22.5, 0)
				.scale(12)
				.lighting(RVs.BLOCK_LIGHTING)
				.withSize(BLOCK_SIZE)
				.at(-1, 2);
		var result = shadowElement.blockWithShadow(() -> getTargetBlock(recipe), blockElement);

		return result
				.onTooltip(() -> BlockPredicateExtensions.getTooltips(getTargetBlock(recipe), recipe.blockPredicate()))
				.onClick(button ->
						rvHelper().buttonToUsageOrRecipe(button)
								.ifPresent(usageOrRecipe -> rvHelper().openPage(getTargetBlock(recipe), usageOrRecipe)))
				.at(TARGET_BLOCK_POSITION)
				.withSize(BLOCK_SIZE);
	}
}
