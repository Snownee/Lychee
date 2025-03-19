package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.rei.LycheeREIPlugin;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.InteractiveWidget;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.compat.rv.category.IBlockCrushingRecipeCategory;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public final class BlockCrushingRecipeCategory extends AbstractLycheeCategory<BlockCrushingRecipe> implements IBlockCrushingRecipeCategory {
	private final Rect2i fallingBlockRect = FALLING_BLOCK_RECT;
	private final Rect2i landingBlockRect = LANDING_BLOCK_RECT;

	public BlockCrushingRecipeCategory(RvCategory<BlockCrushingRecipe> category) {
		super(category);
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<BlockCrushingRecipe> display, Rectangle bounds) {
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));

		var startPoint = new Point(bounds.getX(), bounds.getY() + 4);
		var recipe = display.recipe().value();
		createInfoBadgeIfNeeded(widgets, display, startPoint);
		widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			var x = recipe.getIngredients().isEmpty() ? 41 : 77;
			var anyLandingBlock = BlockPredicateExtensions.isAny(recipe.landingBlock());
			var y = anyLandingBlock ? 45 : 33;

			var ticks = (System.currentTimeMillis() % 2000) / 1000F;
			ticks = Math.min(1, ticks);
			ticks = ticks * ticks * ticks * ticks;

			var matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x, startPoint.y, 0);

			var landingBlock = getLandingBlock(recipe);
			if (landingBlock.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(x + 10.5, y + (anyLandingBlock ? 1 : 16), 0);
				var shadow = 0.6F;
				if (anyLandingBlock) {
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
		}));

		var xCenter = bounds.getCenterX();
		var y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		ingredientGroup(widgets, startPoint, recipe, xCenter - 45 - startPoint.x, y);
		actionGroup(widgets, startPoint, recipe, xCenter + 50 - startPoint.x, y);

		var x = recipe.getIngredients().isEmpty() ? 41 : 77;
		y = BlockPredicateExtensions.isAny(recipe.landingBlock()) ? 45 : 33;
		fallingBlockRect.setPosition(x, y - 35);
		landingBlockRect.setPosition(x, y);

		var widget = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, fallingBlockRect));
		widget.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getFallingBlock(recipe), recipe.blockPredicate()));
		widget.setOnClick(($, button) -> clickBlock(getFallingBlock(recipe), button));
		widgets.add(widget);

		if (!BlockPredicateExtensions.isAny(recipe.landingBlock())) {
			widget = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, landingBlockRect));
			widget.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getLandingBlock(recipe), recipe.landingBlock()));
			widget.setOnClick(($, button) -> clickBlock(getLandingBlock(recipe), button));
			widgets.add(widget);
		}

		var removeActionPosition = getRemoveActionPosition();
		LycheeCategory.addRemoveInputBlock(
				removeActionPosition.x() + startPoint.x,
				removeActionPosition.y() + startPoint.y,
				widgets,
				recipe);

		return widgets;
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

	@Override
	public Rect2i landingBlockRect() {
		return landingBlockRect;
	}
}
