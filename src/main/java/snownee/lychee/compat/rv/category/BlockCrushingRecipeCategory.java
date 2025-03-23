package snownee.lychee.compat.rv.category;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class BlockCrushingRecipeCategory extends AbstractRvCategory<BlockCrushingRecipe> {
	private static final Vector2ic FALLING_BLOCK_SIZE = new Vector2i(20, 35);
	private static final Vector2ic LANDING_BLOCK_SIZE = new Vector2i(20, 20);

	protected BlockCrushingRecipeCategory(RvCategoryType<BlockCrushingRecipe> type, ResourceLocation id, RVCategoryHandler rvHelper) {
		super(type, id, rvHelper);
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<BlockCrushingRecipe> recipeHolder, Vector2i position) {
		var recipe = recipeHolder.value();
		var centerX = position.x() + width() / 2;
		var needSecondLine = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9;
		var y = position.y() + (needSecondLine ? 26 : 28);
		builder.ingredientGroup(recipe, new Vector2i(centerX - 45, y));
		builder.actionGroup(recipe, new Vector2i(centerX + 50, y));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<BlockCrushingRecipe> recipeHolder, Vector2i position) {
		var recipe = recipeHolder.value();

		var landingBlockIsAny = BlockPredicateExtensions.isAny(recipe.landingBlock());

		var xOffset = (recipe.getIngredients().isEmpty() ? 41 : 77) + position.x;
		var yOffset = (landingBlockIsAny ? 45 : 33) + position.y;

		builder.addElement(RenderElement.create((graphics, x, y) -> {
			var ticks = (System.currentTimeMillis() % 2000) / 1000F;
			ticks = Math.min(1, ticks);
			ticks = ticks * ticks * ticks * ticks;

			var matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(x, y, 0);

			var landingBlock = getLandingBlock(recipe);
			if (landingBlock.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(x + 10.5, y + (landingBlockIsAny ? 1 : 16), 0);
				var shadow = 0.6F;
				if (landingBlockIsAny) {
					shadow = 0.2F + ticks * 0.2F;
				}
				matrixStack.scale(shadow, shadow, shadow);
				matrixStack.translate(-26, -5.5, 0);
				AllGuiTextures.SHADOW.render(graphics, 0, 0);
				matrixStack.popPose();
			}

			matrixStack.pushPose();
			matrixStack.translate(x, y - 13, 0);
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
		}).at(xOffset, yOffset));

		var fallingBlockPosition = new Vector2i(xOffset, yOffset - 35);
		var landingBlockPosition = new Vector2i(xOffset, yOffset);

		builder.addElement(new InteractiveRenderElement()
				.<InteractiveRenderElement>at(fallingBlockPosition)
				.<InteractiveRenderElement>withSize(FALLING_BLOCK_SIZE)
				.onTooltip(() -> BlockPredicateExtensions.getTooltips(getFallingBlock(recipe), recipe.blockPredicate()))
				.onClick(button -> rvHandler().buttonToUsageOrRecipe(button)
						.ifPresent(usageOrRecipe -> rvHandler().openPage(getFallingBlock(recipe), usageOrRecipe))));

		if (!landingBlockIsAny) {
			builder.addElement(new InteractiveRenderElement()
					.<InteractiveRenderElement>at(landingBlockPosition)
					.<InteractiveRenderElement>withSize(LANDING_BLOCK_SIZE)
					.onTooltip(() -> BlockPredicateExtensions.getTooltips(getLandingBlock(recipe), recipe.landingBlock()))
					.onClick(button -> rvHandler().buttonToUsageOrRecipe(button)
							.ifPresent(usageOrRecipe -> rvHandler().openPage(getLandingBlock(recipe), usageOrRecipe))));
		}

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = VectorExtensions.offset(LANDING_BLOCK_SIZE, xOffset - 4, yOffset - 8);
			builder.addElement(AbstractRvCategory.getRemoveInputIcon().at(removeActionPosition));
		}
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
