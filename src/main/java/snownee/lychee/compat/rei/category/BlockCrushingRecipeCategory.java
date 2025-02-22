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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.LycheeREIPlugin;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.InteractiveWidget;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipeType;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public final class BlockCrushingRecipeCategory extends AbstractLycheeCategory<BlockCrushingRecipe> {

	public static final Rect2i FALLING_BLOCK_RECT = new Rect2i(0, -35, 20, 35);
	public static final Rect2i LANDING_BLOCK_RECT = new Rect2i(0, 0, 20, 20);
	private final CategoryIdentifier<? extends LycheeDisplay<BlockCrushingRecipe>> categoryIdentifier;
	private final Rect2i fallingBlockRect;
	private final Rect2i landingBlockRect;

	public BlockCrushingRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<BlockCrushingRecipe>> id,
			Renderer icon,
			Rect2i fallingBlockRect,
			Rect2i landingBlockRect) {
		super(id, icon);
		this.categoryIdentifier = id;
		this.fallingBlockRect = fallingBlockRect;
		this.landingBlockRect = landingBlockRect;
	}

	public BlockCrushingRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<BlockCrushingRecipe>> categoryIdentifier, Renderer icon) {
		this(categoryIdentifier, icon, FALLING_BLOCK_RECT, LANDING_BLOCK_RECT);
	}

	@Override
	public int getDisplayWidth(LycheeDisplay<BlockCrushingRecipe> display) {
		return contentWidth();
	}

	@Override
	public int contentWidth() {
		return WIDTH + 20;
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<BlockCrushingRecipe> display, Rectangle bounds) {
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));

		var startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		var recipe = display.recipe();
		drawInfoBadgeIfNeeded(widgets, display, startPoint);
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
				AllGuiTextures.JEI_SHADOW.render(graphics, 0, 0);
				matrixStack.popPose();
			}

			matrixStack.pushPose();
			matrixStack.translate(x, y - 13, 0);
			GuiGameElement.of(getFallingBlock(recipe))
					.scale(15)
					.atLocal(0, ticks * 1.3 - 1.3, 2)
					.rotateBlock(20, 225, 0)
					.lighting(JEIREI.BLOCK_LIGHTING)
					.at(0, 0, 300)
					.render(graphics);
			if (!landingBlock.isAir()) {
				GuiGameElement.of(landingBlock)
						.scale(15)
						.atLocal(0, 1, 2)
						.rotateBlock(20, 225, 0)
						.lighting(JEIREI.BLOCK_LIGHTING)
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

		var reactive = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, fallingBlockRect));
		reactive.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getFallingBlock(recipe), recipe.blockPredicate()));
		reactive.setOnClick(($, button) -> clickBlock(getFallingBlock(recipe), button));
		widgets.add(reactive);

		if (!BlockPredicateExtensions.isAny(recipe.landingBlock())) {
			reactive = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, landingBlockRect));
			reactive.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getLandingBlock(recipe), recipe.landingBlock()));
			reactive.setOnClick(($, button) -> clickBlock(getLandingBlock(recipe), button));
			widgets.add(reactive);
		}

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
	public BlockCrushingRecipeType recipeType() {
		return RecipeTypes.BLOCK_CRUSHING;
	}

	@Override
	public CategoryIdentifier<? extends LycheeDisplay<BlockCrushingRecipe>> getCategoryIdentifier() {return categoryIdentifier;}
}
