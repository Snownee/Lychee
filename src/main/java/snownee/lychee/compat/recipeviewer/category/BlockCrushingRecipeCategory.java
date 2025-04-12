package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import com.mojang.blaze3d.vertex.PoseStack;

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
	private static final int BLOCK_SIZE = 13;

	@Override
	public void setupDecorations(DecorationMapBuilder<BlockCrushingRecipe> mapBuilder) {
		mapBuilder.info(this::infoPosition);
		mapBuilder.consumeBlockInput($ -> VectorExtensions.offset(landingBlockPosition($), BLOCK_SIZE, BLOCK_SIZE - 10));

		mapBuilder.put(
				"falling_block", (builder, recipeHolder) -> {
					var recipe = recipeHolder.value();
					var landingBlockIsAny = landingBlockIsAny(recipe);

					builder.addElement(RenderElement.create((graphics, element) -> {
								var ticks = (System.currentTimeMillis() % 2000) / 1000F;
								ticks = Math.min(1, ticks);
								ticks = ticks * ticks * ticks * ticks;

								var matrixStack = graphics.pose();
								matrixStack.pushPose();

								if (getLandingBlock(recipe).getLightEmission() < 5) {
									matrixStack.pushPose();
									var shadow = 0.5F;
									int y = element.height();
									if (landingBlockIsAny) {
										shadow = 0.2F + ticks * 0.3F;
									} else {
										y += BLOCK_SIZE;
									}
									matrixStack.translate(element.width() / 2F, y, 0);
									matrixStack.scale(shadow, shadow, shadow);
									matrixStack.translate(-AllGuiTextures.SHADOW.width * 0.5F, -AllGuiTextures.SHADOW.height * 0.5F, 0);
									AllGuiTextures.SHADOW.render(graphics);
									matrixStack.popPose();
								}

								matrixStack.pushPose();
								GuiGameElement.of(getFallingBlock(recipe))
										.scale(BLOCK_SIZE)
										.atLocal(0, ticks * 1.3 + 0.4, 2)
										.rotateBlock(20, 225, 0)
										.lighting(RVs.BLOCK_LIGHTING)
										.atZ(300)
										.render(graphics);
								matrixStack.popPose();

								matrixStack.popPose();
							})
							.at(fallingBlockPosition(recipe))
							.withSize(BLOCK_SIZE, FALLING_BLOCK_HEIGHT));

					RvHelper helper = builder.helper();
					builder.addElement(new InteractiveRenderElement()
							.at(fallingBlockPosition(recipe))
							.<InteractiveRenderElement>withSize(BLOCK_SIZE, FALLING_BLOCK_HEIGHT)
							.onTooltip(() -> BlockPredicateExtensions.getTooltips(getFallingBlock(recipe), recipe.blockPredicate()))
							.onInput(helper.inputOnBlock(() -> getFallingBlock(recipe))));
				});

		mapBuilder.condition("landing_block", $ -> !landingBlockIsAny($));
		mapBuilder.put(
				"landing_block", (builder, recipeHolder) -> {
					var recipe = recipeHolder.value();
					Vector2f landingBlockPosition = landingBlockPosition(recipe);

					builder.addElement(RenderElement.create((graphics, element) -> {
						var matrixStack = graphics.pose();
						matrixStack.pushPose();
						GuiGameElement.of(getLandingBlock(recipe))
								.scale(BLOCK_SIZE)
								.rotateBlock(20, 225, 0)
								.lighting(RVs.BLOCK_LIGHTING)
								.render(graphics);
						matrixStack.popPose();
					}).at(landingBlockPosition));

					RvHelper helper = builder.helper();
					builder.addElement(new InteractiveRenderElement().at(landingBlockPosition)
							.<InteractiveRenderElement>withSize(BLOCK_SIZE)
							.onTooltip(() -> BlockPredicateExtensions.getTooltips(getLandingBlock(recipe), recipe.landingBlock()))
							.onInput(helper.inputOnBlock(() -> getLandingBlock(recipe))));
				});
	}

	@Override
	public Vector2fc infoPosition(BlockCrushingRecipe recipe) {
		return VectorExtensions.offset(
				fallingBlockPosition(recipe),
				BLOCK_SIZE + 4,
				(FALLING_BLOCK_HEIGHT - InfoElementHelper.INFO_SIZE) / 2f);
	}

	protected Vector2f landingBlockPosition(BlockCrushingRecipe recipe) {
		var xOffset = recipe.getIngredients().isEmpty() ? 40 : (width - BLOCK_SIZE) / 2;
		return new Vector2f(xOffset, 38);
	}

	protected Vector2f fallingBlockPosition(BlockCrushingRecipe recipe) {
		var xOffset = landingBlockPosition(recipe).x;
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

	public static InteractiveRenderElement icon(RenderElement element) {
		return InteractiveRenderElement.create(graphics -> {
			var ticks = (System.currentTimeMillis() % 3000) / 1000F;
			float pos = 0;
			if (ticks < 1.5F) {
				pos = ticks * ticks * ticks * ticks;
				pos *= 15;
				if (ticks > 1) {
					pos -= 1.5F * 1.5F * 1.5F * 1.5F * 15;
				}
			}
			PoseStack pose = graphics.pose();
			pose.translate(0, pos, 0);
			element.render(graphics);
		}).withScissors(true).at(element.position).withSize(element.size);
	}
}
