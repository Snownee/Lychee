package snownee.lychee.compat.rei.category;

import java.util.List;
import java.util.function.Supplier;

import org.joml.Vector2i;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.rei.LycheeREIPlugin;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.InteractiveWidget;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class DripstoneRecipeCategory extends AbstractLycheeCategory<DripstoneRecipe> {

	private final Rect2i sourceBlockRect = new Rect2i(23, 1, 16, 16);
	private final Rect2i targetBlockRect = new Rect2i(23, 43, 16, 16);
	protected Supplier<Vector2i> removeActionPosition =
			Suppliers.memoize(() -> new Vector2i(
					targetBlockRect.getX() + targetBlockRect.getWidth() - 4,
					targetBlockRect.getY() + targetBlockRect.getHeight() - 8));

	public DripstoneRecipeCategory(CategoryIdentifier<? extends LycheeDisplay<DripstoneRecipe>> id, RvCategory<DripstoneRecipe> category) {
		super(id, category);
		infoRect.setX(-10);
	}

	private static void drawBlock(BlockState state, GuiGraphics graphics, double localX, double localY, double localZ) {
		GuiGameElement.of(state)
				.scale(12)
				.lighting(RVs.BLOCK_LIGHTING)
				.atLocal(localX, localY, localZ)
				.rotateBlock(12.5, -22.5, 0)
				.render(graphics);
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<DripstoneRecipe> display, Rectangle bounds) {
		var startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		var recipe = display.recipe().value();
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));
		createInfoBadgeIfNeeded(widgets, display, startPoint);
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
				AllGuiTextures.SHADOW.render(graphics, 0, 0);
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

		var widget = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, sourceBlockRect));
		widget.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getSourceBlock(recipe), recipe.sourceBlock()));
		widget.setOnClick(($, button) -> clickBlock(getSourceBlock(recipe), button));
		widgets.add(widget);

		widget = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, targetBlockRect));
		widget.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getTargetBlock(recipe), recipe.blockPredicate()));
		widget.setOnClick(($, button) -> clickBlock(getTargetBlock(recipe), button));
		widgets.add(widget);

		LycheeCategory.addRemoveInputBlock(
				removeActionPosition.get().x + startPoint.x,
				removeActionPosition.get().y + startPoint.y,
				widgets,
				recipe);

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
}
