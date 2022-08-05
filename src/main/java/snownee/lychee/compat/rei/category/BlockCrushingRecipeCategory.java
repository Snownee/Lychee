package snownee.lychee.compat.rei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.block_crushing.BlockCrushingContext;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.ReactiveWidget;
import snownee.lychee.compat.rei.display.BlockCrushingDisplay;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public class BlockCrushingRecipeCategory extends BaseREICategory<BlockCrushingContext, BlockCrushingRecipe, BlockCrushingDisplay> {

	public static final Rect2i fallingBlockRect = new Rect2i(5, -35, 20, 35);
	public static final Rect2i landingBlockRect = new Rect2i(5, 0, 20, 20);

	public BlockCrushingRecipeCategory(LycheeRecipeType<BlockCrushingContext, BlockCrushingRecipe> recipeType) {
		super(recipeType);
		icon = EntryStacks.of(Items.ANVIL);
	}

	@Override
	public CategoryIdentifier<? extends BlockCrushingDisplay> getCategoryIdentifier() {
		return REICompat.BLOCK_CRUSHING;
	}

	@Override
	public int getDisplayWidth(BlockCrushingDisplay display) {
		return getRealWidth();
	}

	@Override
	public int getRealWidth() {
		return width + 20;
	}

	@Override
	public List<Widget> setupDisplay(BlockCrushingDisplay display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - getRealWidth() / 2, bounds.getY() + 4);
		BlockCrushingRecipe recipe = display.recipe;
		List<Widget> widgets = super.setupDisplay(display, bounds);
		widgets.add(Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
			int x = recipe.getIngredients().isEmpty() ? 36 : 72;
			boolean anyLandingBlock = recipe.getLandingBlock() == BlockPredicate.ANY;
			int y = anyLandingBlock ? 45 : 33;

			float ticks = (System.currentTimeMillis() % 2000) / 1000F;
			ticks = Math.min(1, ticks);
			ticks = ticks * ticks * ticks * ticks;

			matrixStack.pushPose();
			matrixStack.translate(startPoint.x, startPoint.y, 0);

			BlockState landingBlock = getLandingBlock(recipe);
			if (landingBlock.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(x + (anyLandingBlock ? 16 : 18), y + (anyLandingBlock ? 1 : 18), 0);
				float shadow = 0.6F;
				if (anyLandingBlock) {
					shadow = 0.2F + ticks * 0.2F;
				}
				matrixStack.scale(shadow, shadow, shadow);
				matrixStack.translate(-26, -5.5, 0);
				AllGuiTextures.JEI_SHADOW.render(matrixStack, 0, 0);
				matrixStack.popPose();
			}

			matrixStack.pushPose();
			matrixStack.translate(0, 0, 300);
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
			matrixStack.translate(x, y, 0);
			GuiGameElement.of(getFallingBlock(recipe)).scale(15).atLocal(0, ticks * 1.3 - 1.3, 2).rotateBlock(0, 45, 0).render(matrixStack);
			if (!landingBlock.isAir()) {
				GuiGameElement.of(landingBlock).scale(15).atLocal(0, 1, 2).rotateBlock(0, 45, 0).render(matrixStack);
			}
			matrixStack.popPose();
			matrixStack.popPose();
		}));

		int xCenter = bounds.getCenterX();
		int y = recipe.getIngredients().size() > 9 || recipe.getShowingPostActions().size() > 9 ? 26 : 28;
		ingredientGroup(widgets, startPoint, recipe, xCenter - 45 - startPoint.x, y, false);
		actionGroup(widgets, startPoint, recipe, xCenter + 50 - startPoint.x, y);

		int x = recipe.getIngredients().isEmpty() ? 36 : 72;
		y = recipe.getLandingBlock() == BlockPredicate.ANY ? 45 : 33;
		fallingBlockRect.setPosition(x + 5, y - 35);
		landingBlockRect.setPosition(x + 5, y);

		ReactiveWidget reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, fallingBlockRect));
		reactive.setTooltipFunction($ -> {
			List<Component> list = BlockPredicateHelper.getTooltips(getFallingBlock(recipe), recipe.getBlock());
			return list.toArray(new Component[0]);
		});
		reactive.setOnClick(($, button) -> {
			clickBlock(getFallingBlock(recipe), button);
		});
		widgets.add(reactive);

		if (recipe.getLandingBlock() != BlockPredicate.ANY) {
			reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, landingBlockRect));
			reactive.setTooltipFunction($ -> {
				List<Component> list = BlockPredicateHelper.getTooltips(getLandingBlock(recipe), recipe.getLandingBlock());
				return list.toArray(new Component[0]);
			});
			reactive.setOnClick(($, button) -> {
				clickBlock(getLandingBlock(recipe), button);
			});
			widgets.add(reactive);
		}

		return widgets;
	}

	private BlockState getFallingBlock(BlockCrushingRecipe recipe) {
		return LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.ANVIL.defaultBlockState(), 2000);
	}

	private BlockState getLandingBlock(BlockCrushingRecipe recipe) {
		return LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getLandingBlock()), Blocks.AIR.defaultBlockState(), 2000);
	}

}
