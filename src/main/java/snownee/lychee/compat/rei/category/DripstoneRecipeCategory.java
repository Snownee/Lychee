package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.LycheeREIPlugin;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.InteractiveWidget;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class DripstoneRecipeCategory extends AbstractLycheeCategory<DripstoneRecipe> {

	private final Rect2i sourceBlockRect = new Rect2i(23, 1, 16, 16);
	private final Rect2i targetBlockRect = new Rect2i(23, 43, 16, 16);

	public DripstoneRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<DripstoneRecipe>> categoryIdentifier,
			Renderer icon
	) {
		super(categoryIdentifier, icon);
		infoRect.setX(-10);
	}


	private static void drawBlock(BlockState state, GuiGraphics graphics, double localX, double localY, double localZ) {
		GuiGameElement.of(state)
				.scale(12)
				.lighting(JEIREI.BLOCK_LIGHTING)
				.atLocal(localX, localY, localZ)
				.rotateBlock(12.5, -22.5, 0)
				.render(graphics);
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<DripstoneRecipe> display, Rectangle bounds) {
		var startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		var recipe = display.recipe();
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));
		drawInfoBadgeIfNeeded(widgets, display, startPoint);
		widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			var matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x, startPoint.y, 0);

			var targetBlock = getTargetBlock(recipe);
			if (targetBlock.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(31, 56, 0);
				var shadow = 0.5F;
				matrixStack.scale(shadow, shadow, shadow);
				matrixStack.translate(-26, -5.5, 0);
				AllGuiTextures.JEI_SHADOW.render(graphics, 0, 0);
				matrixStack.popPose();
			}

			matrixStack.pushPose();
			matrixStack.translate(22, 24, 300);
			drawBlock(getSourceBlock(recipe), graphics, 0, -2, 0);
			drawBlock(Blocks.DRIPSTONE_BLOCK.defaultBlockState(), graphics, 0, -1, 0);
			drawBlock(
					Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN),
					graphics,
					0,
					0,
					0);
			drawBlock(targetBlock, graphics, 0, 1.5, 0);
			matrixStack.popPose();

			matrixStack.popPose();
		}));

		var y = recipe.conditions().showingCount() > 9 ? 26 : 28;
		actionGroup(widgets, startPoint, recipe, contentWidth() - 24, y);

		var reactive = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, sourceBlockRect));
		reactive.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getSourceBlock(recipe), recipe.sourceBlock()));
		reactive.setOnClick(($, button) -> clickBlock(getSourceBlock(recipe), button));
		widgets.add(reactive);

		reactive = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, targetBlockRect));
		reactive.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getTargetBlock(recipe), recipe.blockPredicate()));
		reactive.setOnClick(($, button) -> clickBlock(getTargetBlock(recipe), button));
		widgets.add(reactive);

		return widgets;
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
	public LycheeRecipeType<? extends DripstoneRecipe> recipeType() {
		return RecipeTypes.DRIPSTONE_DRIPPING;
	}
}
