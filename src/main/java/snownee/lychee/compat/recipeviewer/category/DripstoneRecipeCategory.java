package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Supplier;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class DripstoneRecipeCategory extends AbstractRvCategory<DripstoneRecipe> {
	private static final int BLOCK_SIZE = 16;
	private static final Vector2fc SOURCE_BLOCK_POSITION = new Vector2f(22, 0);
	private static final Vector2fc TARGET_BLOCK_POSITION = new Vector2f(22, 36);

	protected DripstoneRecipeCategory(
			RvCategoryType<DripstoneRecipe> type,
			ResourceLocation id,
			RVHelper rvHandler
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
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<DripstoneRecipe> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();
		var centerX = position.x() + (float) width() / 2;
		var needSecondLine = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9;
		var y = position.y() + (needSecondLine ? 26 : 28);
		builder.actionGroup(recipe, new Vector2f(centerX - 24, y));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<DripstoneRecipe> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		builder.addElement(RenderElement.create((graphics) -> {
			var matrixStack = graphics.pose();
			matrixStack.pushPose();

			var targetBlock = getTargetBlock(recipe);
			if (targetBlock.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(31, 56, 0);
				var shadow = 0.5F;
				matrixStack.scale(shadow, shadow, shadow);
				matrixStack.translate(-26, -5.5, 0);
				AllGuiTextures.SHADOW.render(graphics);
				matrixStack.popPose();
			}

			getBlockElement(recipe, () -> getSourceBlock(recipe)).at(SOURCE_BLOCK_POSITION).render(graphics);
			getBlockElement(recipe, Blocks.DRIPSTONE_BLOCK::defaultBlockState).render(graphics, 22, 12);
			getBlockElement(
					recipe,
					() -> Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
			).render(graphics, 22, 24);
			getBlockElement(recipe, () -> targetBlock).at(TARGET_BLOCK_POSITION).render(graphics);

			matrixStack.popPose();
		}).at(position));

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = VectorExtensions.offset(TARGET_BLOCK_POSITION, -4, -8);
			builder.addElement(AbstractRvCategory.getRemoveInputIcon().at(removeActionPosition));
		}
	}

	protected RenderElement getBlockElement(
			DripstoneRecipe recipe,
			Supplier<BlockState> stateSupplier) {
		Supplier<RenderElement> blockElement = () -> GuiGameElement.of(stateSupplier.get())
				.scale(12)
				.lighting(RVs.BLOCK_LIGHTING)
				.rotateBlock(12.5, -22.5, 0);
		return new InteractiveRenderElement((InteractiveRenderElement element) -> blockElement.get())
				.onTooltip(() -> BlockPredicateExtensions.getTooltips(stateSupplier.get(), recipe.blockPredicate()))
				.onClick(button -> rvHelper().buttonToUsageOrRecipe(button)
						.ifPresent(usageOrRecipe -> rvHelper().openPage(stateSupplier.get(), usageOrRecipe)))
				.withSize(BLOCK_SIZE);
	}
}
