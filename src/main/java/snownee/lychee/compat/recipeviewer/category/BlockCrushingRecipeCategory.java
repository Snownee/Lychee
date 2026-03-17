package snownee.lychee.compat.recipeviewer.category;

import java.util.Objects;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class BlockCrushingRecipeCategory extends RvCategory<BlockCrushingRecipe> {
	private static final int FALLING_BLOCK_HEIGHT = 35;
	private static final int BLOCK_SIZE = 13;

	public BlockCrushingRecipeCategory() {
		super(RecipeTypes.BLOCK_CRUSHING);
	}

	@Override
	public void setupDecorations(DecorationMapBuilder<BlockCrushingRecipe> mapBuilder) {
		mapBuilder.info(this::infoPosition);
		mapBuilder.consumeBlockInput($ -> VectorExtensions.offset(landingBlockPosition($), BLOCK_SIZE, BLOCK_SIZE - 10));

		mapBuilder.put(
				"falling_block", (builder, recipeHolder) -> {
					var recipe = recipeHolder.value();
					var needLandingBlock = needLandingBlock(recipe);

					var fallingBlockPosition = fallingBlockPosition(recipe);
					var landingBlockPosition = landingBlockPosition(recipe);
					builder.addElement(RenderElement.createSimple((graphics, element) -> {
								var ticks = (System.currentTimeMillis() % 2000) / 1000F;
								ticks = Math.min(1, ticks);
								ticks = ticks * ticks * ticks * ticks;

								var matrixStack = graphics.pose();
								if (getLandingBlock(recipe).getLightEmission() < 5) {
									matrixStack.pushMatrix();
									var scale = 0.5F;
									var x = landingBlockPosition.x() + element.width() / 2F;
									var y = fallingBlockPosition.y + FALLING_BLOCK_HEIGHT;
									if (needLandingBlock) {
										y += BLOCK_SIZE;
									} else {
										scale = 0.2F + ticks * 0.3F;
									}
									matrixStack.translate(x, y);
									matrixStack.scale(scale);
									matrixStack.translate(-AllGuiTextures.SHADOW.width * 0.5F, AllGuiTextures.SHADOW.height * 0.5F);
									AllGuiTextures.SHADOW.render(graphics);
									matrixStack.popMatrix();
								}
							})
							.sortOrder(300));

					builder.addElement(RenderElement.createSimple((graphics, element) -> {
								var ticks = (System.currentTimeMillis() % 2000) / 1000F;
								ticks = Math.min(1, ticks);
								ticks = ticks * ticks * ticks * ticks;

								GuiGameElement.of(getFallingBlock(recipe))
										.scale(BLOCK_SIZE)
										.atLocal(0.15, ticks * 1.3, 2)
										.rotateBlock(20, 225, 0)
										.debugOutline(graphics, 0xFFFFFFFF)
										.withSize(BLOCK_SIZE, FALLING_BLOCK_HEIGHT)
										.render(graphics);
							})
							.sortOrder(200)
							.at(fallingBlockPosition)
							.withSize(BLOCK_SIZE, FALLING_BLOCK_HEIGHT));

					RvHelper helper = builder.helper();
					builder.addElement(new InteractiveRenderElement()
							.at(fallingBlockPosition)
							.<InteractiveRenderElement>withSize(BLOCK_SIZE, FALLING_BLOCK_HEIGHT)
							.onTooltip(() -> BlockPredicateExtensions.getTooltips(getFallingBlock(recipe), recipe.blockPredicate(), helper))
							.onInput(helper.inputOnBlock(() -> getFallingBlock(recipe))));
				});

		mapBuilder.condition("landing_block", this::needLandingBlock);
		mapBuilder.put(
				"landing_block", (builder, recipeHolder) -> {
					var recipe = recipeHolder.value();
					Vector2f landingBlockPosition = landingBlockPosition(recipe);

					builder.addElement(GuiGameElement.of(getLandingBlock(recipe))
							.scale(BLOCK_SIZE)
							.rotateBlock(20, 225, 0)
							.at(landingBlockPosition)
							.sortOrder(250)
					);

					RvHelper helper = builder.helper();
					builder.addElement(new InteractiveRenderElement().at(landingBlockPosition)
							.<InteractiveRenderElement>withSize(BLOCK_SIZE)
							.onTooltip(() -> BlockPredicateExtensions.getTooltips(getLandingBlock(recipe), recipe.landingBlock(), helper))
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
		var xOffset = recipe.sizedIngredients().isEmpty() ? 40 : (width - BLOCK_SIZE) / 2;
		return new Vector2f(xOffset, 38);
	}

	protected Vector2f fallingBlockPosition(BlockCrushingRecipe recipe) {
		var xOffset = landingBlockPosition(recipe).x;
		var yOffset = needLandingBlock(recipe) ? 34 : 46;
		return new Vector2f(xOffset, yOffset - FALLING_BLOCK_HEIGHT);
	}

	protected boolean needLandingBlock(BlockCrushingRecipe recipe) {
		return !BlockPredicateExtensions.isAny(recipe.landingBlock());
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
		return RenderElement.create(
				element.at(element.position).withSize(element.size),
				$ -> {
					var ticks = (System.currentTimeMillis() % 3000) / 1000F;
					float pos = 0;
					if (ticks < 1.5F) {
						pos = ticks * ticks * ticks * ticks;
						pos *= 15;
						if (ticks > 1) {
							pos -= 1.5F * 1.5F * 1.5F * 1.5F * 15;
						}
					}
					RenderElement wrappedElement = (RenderElement) Objects.requireNonNull($.getWrappedElement());
					wrappedElement.at($.x(), $.y() + pos);
				}).withScissors(true);
	}
}
