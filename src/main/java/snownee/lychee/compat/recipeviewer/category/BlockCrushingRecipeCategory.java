package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class BlockCrushingRecipeCategory extends RvCategory<BlockCrushingRecipe> {
	private static final int FALLING_BLOCK_HEIGHT = 35;
	private static final int BLOCK_SIZE = 20;

	@Override
	public void setupDecorations(DecorationMapBuilder<BlockCrushingRecipe> mapBuilder) {
		mapBuilder.info(this::infoPosition);
		mapBuilder.consumeBlockInput($ -> VectorExtensions.offset(landingBlockPosition($), BLOCK_SIZE - 4, BLOCK_SIZE - 10));

		mapBuilder.put(
				"falling_block", (builder, recipeHolder) -> {
					var recipe = recipeHolder.value();
					var landingBlockIsAny = landingBlockIsAny(recipe);
					var xOffset = recipe.getIngredients().isEmpty() ? 40 : 76;
					var yOffset = landingBlockIsAny ? 50 : 38;

					builder.addElement(RenderElement.create((graphics, element) -> {
						var ticks = (System.currentTimeMillis() % 2000) / 1000F;
						ticks = Math.min(1, ticks);
						ticks = ticks * ticks * ticks * ticks;

						var matrixStack = graphics.pose();
						matrixStack.pushPose();

						if (getLandingBlock(recipe).getLightEmission() < 5) {
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
						matrixStack.popPose();

						matrixStack.popPose();
					}).at(xOffset, yOffset));

					RvHelper helper = builder.helper();
					builder.addElement(new InteractiveRenderElement().at(fallingBlockPosition(recipe))
							.<InteractiveRenderElement>withSize(
									BLOCK_SIZE,
									FALLING_BLOCK_HEIGHT)
							.onTooltip(() -> BlockPredicateExtensions.getTooltips(getFallingBlock(recipe), recipe.blockPredicate()))
							.onClick(button -> helper.buttonToUsageOrRecipe(button)
									.ifPresent(usageOrRecipe -> helper.openPage(getFallingBlock(recipe), usageOrRecipe))));
				});

		mapBuilder.condition("landing_block", $ -> !landingBlockIsAny($));
		mapBuilder.put(
				"landing_block", (builder, recipeHolder) -> {
					var recipe = recipeHolder.value();
					Vector2f landingBlockPosition = landingBlockPosition(recipe);

					builder.addElement(RenderElement.create((graphics, element) -> {
						var matrixStack = graphics.pose();
						matrixStack.pushPose();
						matrixStack.translate(0, -13, 0);
						GuiGameElement.of(getLandingBlock(recipe))
								.scale(15)
								.atLocal(0, 1, 2)
								.rotateBlock(20, 225, 0)
								.lighting(RVs.BLOCK_LIGHTING)
								.render(graphics);
						matrixStack.popPose();
					}).at(landingBlockPosition));

					RvHelper helper = builder.helper();
					builder.addElement(new InteractiveRenderElement().at(landingBlockPosition)
							.<InteractiveRenderElement>withSize(BLOCK_SIZE)
							.onTooltip(() -> BlockPredicateExtensions.getTooltips(getLandingBlock(recipe), recipe.landingBlock()))
							.onClick(button -> helper.buttonToUsageOrRecipe(button)
									.ifPresent(usageOrRecipe -> helper.openPage(getLandingBlock(recipe), usageOrRecipe))));
				});
	}

	@Override
	public Vector2fc infoPosition(BlockCrushingRecipe recipe) {
		return VectorExtensions.offset(fallingBlockPosition(recipe), BLOCK_SIZE, (FALLING_BLOCK_HEIGHT - InfoElementHelper.INFO_SIZE) / 2f);
	}

	protected Vector2f landingBlockPosition(BlockCrushingRecipe recipe) {
		var xOffset = recipe.getIngredients().isEmpty() ? 40 : 76;
		return new Vector2f(xOffset, 38);
	}

	protected Vector2f fallingBlockPosition(BlockCrushingRecipe recipe) {
		var xOffset = recipe.getIngredients().isEmpty() ? 40 : 76;
		var yOffset = landingBlockIsAny(recipe) ? 50 : 38;
		return new Vector2f(xOffset, yOffset - FALLING_BLOCK_HEIGHT);
	}

	protected boolean landingBlockIsAny(BlockCrushingRecipe recipe) {
		return BlockPredicateExtensions.isAny(recipe.landingBlock());
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
