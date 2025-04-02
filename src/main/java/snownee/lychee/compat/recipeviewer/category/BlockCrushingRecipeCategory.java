package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

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
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class BlockCrushingRecipeCategory extends AbstractRvCategory<BlockCrushingRecipe> {
	private static final int FALLING_BLOCK_HEIGHT = 35;
	private static final int BLOCK_SIZE = 20;

	protected BlockCrushingRecipeCategory(RvCategoryType<BlockCrushingRecipe> type, ResourceLocation id, RVHelper rvHelper) {
		super(type, id, rvHelper);
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<BlockCrushingRecipe> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();
		builder.ingredientGroup(recipe, new Vector2f(27, 28));
		builder.actionGroup(recipe, new Vector2f(width() - 29, 28));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<BlockCrushingRecipe> recipeHolder, Vector2fc position) {
		var recipe = recipeHolder.value();

		var landingBlockIsAny = BlockPredicateExtensions.isAny(recipe.landingBlock());

		var xOffset = (recipe.getIngredients().isEmpty() ? 40 : 76);
		var yOffset = (landingBlockIsAny ? 50 : 38);

		var landingBlockPosition = new Vector2f(xOffset, 38);
		var fallingBlockPosition = new Vector2f(xOffset, yOffset - FALLING_BLOCK_HEIGHT);

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).at(infoPosition(fallingBlockPosition)).offset(position));
		}

		builder.addElement(RenderElement.create((graphics, element) -> {
			var ticks = (System.currentTimeMillis() % 2000) / 1000F;
			ticks = Math.min(1, ticks);
			ticks = ticks * ticks * ticks * ticks;

			var matrixStack = graphics.pose();
			matrixStack.pushPose();

			var landingBlock = getLandingBlock(recipe);
			if (landingBlock.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(10.5, (landingBlockIsAny ? 1 : 16), 0);
				var shadow = 0.6F;
				if (landingBlockIsAny) {
					shadow = 0.2F + ticks * 0.2F;
				}
				matrixStack.scale(shadow, shadow, shadow);
				matrixStack.translate(-26, -5.5, 0);
				AllGuiTextures.SHADOW.render(graphics);
				matrixStack.popPose();
			}

			matrixStack.pushPose();
			matrixStack.translate(0, -13, 0);
			GuiGameElement.of(getFallingBlock(recipe))
					.scale(15)
					.atLocal(0, ticks * 1.3 - 1.3, 2)
					.rotateBlock(20, 225, 0)
					.lighting(RVs.BLOCK_LIGHTING)
					.at(0, 0, 300)
					.render(graphics);
			if (!landingBlock.isAir()) {
				GuiGameElement.of(landingBlock)
						.scale(15)
						.atLocal(0, 1, 2)
						.rotateBlock(20, 225, 0)
						.lighting(RVs.BLOCK_LIGHTING)
						.render(graphics);
			}
			matrixStack.popPose();

			matrixStack.popPose();
		}).at(xOffset, yOffset).offset(position));

		builder.addElement(new InteractiveRenderElement()
				.at(fallingBlockPosition)
				.offset(position)
				.<InteractiveRenderElement>withSize(BLOCK_SIZE, FALLING_BLOCK_HEIGHT)
				.onTooltip(() -> BlockPredicateExtensions.getTooltips(getFallingBlock(recipe), recipe.blockPredicate()))
				.onClick(button -> rvHelper().buttonToUsageOrRecipe(button)
						.ifPresent(usageOrRecipe -> rvHelper().openPage(getFallingBlock(recipe), usageOrRecipe))));

		if (!landingBlockIsAny) {
			builder.addElement(new InteractiveRenderElement()
					.at(landingBlockPosition)
					.offset(position)
					.<InteractiveRenderElement>withSize(BLOCK_SIZE)
					.onTooltip(() -> BlockPredicateExtensions.getTooltips(getLandingBlock(recipe), recipe.landingBlock()))
					.onClick(button -> rvHelper().buttonToUsageOrRecipe(button)
							.ifPresent(usageOrRecipe -> rvHelper().openPage(getLandingBlock(recipe), usageOrRecipe))));
		}

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = VectorExtensions.offset(
					landingBlockPosition,
					BLOCK_SIZE - 4,
					BLOCK_SIZE - 8);
			builder.addElement(AbstractRvCategory.getRemoveInputIcon().at(removeActionPosition).offset(position));
		}
	}

	public Vector2fc infoPosition(Vector2fc fallingBlockPosition) {
		return VectorExtensions.offset(fallingBlockPosition, BLOCK_SIZE, (FALLING_BLOCK_HEIGHT - InfoElementHelper.INFO_SIZE) / 2f);
	}

	private BlockState getFallingBlock(BlockCrushingRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}

	private BlockState getLandingBlock(BlockCrushingRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.landingBlock()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}
}
