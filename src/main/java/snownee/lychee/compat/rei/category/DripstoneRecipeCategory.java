package snownee.lychee.compat.rei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.ReactiveWidget;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.dripstone_dripping.DripstoneContext;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.dripstone_dripping.DripstoneRecipeType;
import snownee.lychee.util.LUtil;

public class DripstoneRecipeCategory extends BaseREICategory<DripstoneContext, DripstoneRecipe, BaseREIDisplay<DripstoneRecipe>> {

	private Rect2i sourceBlockRect = new Rect2i(23, 1, 16, 16);
	private Rect2i targetBlockRect = new Rect2i(23, 43, 16, 16);

	public DripstoneRecipeCategory(DripstoneRecipeType recipeType) {
		super(recipeType);
		infoRect.setX(-10);
	}

	@Override
	public List<Widget> setupDisplay(BaseREIDisplay<DripstoneRecipe> display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - getRealWidth() / 2, bounds.getY() + 4);
		DripstoneRecipe recipe = display.recipe;
		List<Widget> widgets = super.setupDisplay(display, bounds);
		drawInfoBadge(widgets, display, startPoint);
		widgets.add(Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x, startPoint.y, 0);

			BlockState targetBlock = getTargetBlock(recipe);
			if (targetBlock.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(31, 56, 0);
				float shadow = 0.5F;
				matrixStack.scale(shadow, shadow, shadow);
				matrixStack.translate(-26, -5.5, 0);
				AllGuiTextures.JEI_SHADOW.render(matrixStack, 0, 0);
				matrixStack.popPose();
			}

			matrixStack.pushPose();
			matrixStack.translate(22, 24, 300);
			drawBlock(getSourceBlock(recipe), matrixStack, 0, -2, 0);
			drawBlock(Blocks.DRIPSTONE_BLOCK.defaultBlockState(), matrixStack, 0, -1, 0);
			drawBlock(Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN), matrixStack, 0, 0, 0);
			drawBlock(targetBlock, matrixStack, 0, 1.5, 0);
			matrixStack.popPose();

			matrixStack.popPose();
		}));

		int y = recipe.showingActionsCount() > 9 ? 26 : 28;
		actionGroup(widgets, startPoint, recipe, getRealWidth() - 24, y);

		ReactiveWidget reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, sourceBlockRect));
		reactive.setTooltipFunction($ -> {
			List<Component> list = BlockPredicateHelper.getTooltips(getSourceBlock(recipe), recipe.getSourceBlock());
			return list.toArray(new Component[0]);
		});
		reactive.setOnClick(($, button) -> {
			clickBlock(getSourceBlock(recipe), button);
		});
		widgets.add(reactive);

		reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, targetBlockRect));
		reactive.setTooltipFunction($ -> {
			List<Component> list = BlockPredicateHelper.getTooltips(getTargetBlock(recipe), recipe.getBlock());
			return list.toArray(new Component[0]);
		});
		reactive.setOnClick(($, button) -> {
			clickBlock(getTargetBlock(recipe), button);
		});
		widgets.add(reactive);

		return widgets;
	}

	private static void drawBlock(BlockState state, PoseStack matrixStack, double localX, double localY, double localZ) {
		GuiGameElement.of(state).scale(12).lighting(JEIREI.BLOCK_LIGHTING).atLocal(localX, localY, localZ).rotateBlock(12.5, -22.5, 0).render(matrixStack);
	}

	private BlockState getSourceBlock(DripstoneRecipe recipe) {
		return LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getSourceBlock()), Blocks.AIR.defaultBlockState(), 2000);
	}

	private BlockState getTargetBlock(DripstoneRecipe recipe) {
		return LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.AIR.defaultBlockState(), 2000);
	}

	@Override
	public Renderer createIcon(List<DripstoneRecipe> recipes) {
		return EntryStacks.of(Items.POINTED_DRIPSTONE);
	}

}
