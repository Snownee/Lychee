package snownee.lychee.compat.jei.category;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphics;
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
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.dripstone_dripping.DripstoneContext;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.util.CommonProxy;

public class DripstoneRecipeCategory extends BaseJEICategory<DripstoneContext, DripstoneRecipe> {

	private Rect2i sourceBlockRect = new Rect2i(23, 1, 16, 16);
	private Rect2i targetBlockRect = new Rect2i(23, 43, 16, 16);

	public DripstoneRecipeCategory(LycheeRecipeType<DripstoneContext, DripstoneRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper, List<DripstoneRecipe> recipes) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.POINTED_DRIPSTONE.getDefaultInstance());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DripstoneRecipe recipe, IFocusGroup focuses) {
		int y = recipe.conditions().showingCount() > 9 ? 26 : 28;
		actionGroup(builder, recipe, getWidth() - 28, y);
		addBlockIngredients(builder, recipe);
	}

	@Override
	public void draw(DripstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		drawInfoBadgeIfNeeded(recipe, graphics, mouseX, mouseY);
		BlockState sourceBlock = CommonProxy.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getSourceBlock()), Blocks.AIR.defaultBlockState(), 2000);
		BlockState targetBlock = CommonProxy.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.AIR.defaultBlockState(), 2000);

		PoseStack matrixStack = graphics.pose();
		if (targetBlock.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(31, 56, 0);
			float shadow = 0.5F;
			matrixStack.scale(shadow, shadow, shadow);
			matrixStack.translate(-26, -5.5, 0);
			AllGuiTextures.JEI_SHADOW.render(graphics, 0, 0);
			matrixStack.popPose();
		}

		matrixStack.pushPose();
		matrixStack.translate(22, 24, 300);
		drawBlock(sourceBlock, graphics, 0, -2, 0);
		drawBlock(Blocks.DRIPSTONE_BLOCK.defaultBlockState(), graphics, 0, -1, 0);
		drawBlock(Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN), graphics, 0, 0, 0);
		drawBlock(targetBlock, graphics, 0, 1.5, 0);
		matrixStack.popPose();
	}

	private static void drawBlock(BlockState state, GuiGraphics graphics, double localX, double localY, double localZ) {
		GuiGameElement.of(state).scale(12).lighting(JEIREI.BLOCK_LIGHTING).atLocal(localX, localY, localZ).rotateBlock(12.5, 202.5, 0).render(graphics);
	}

	@Override
	public List<Component> getTooltipStrings(DripstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		int x = (int) mouseX;
		int y = (int) mouseY;
		if (sourceBlockRect.contains(x, y)) {
			BlockState sourceBlock = CommonProxy.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getSourceBlock()), Blocks.AIR.defaultBlockState(), 2000);
			return BlockPredicateHelper.getTooltips(sourceBlock, recipe.getSourceBlock());
		}
		if (targetBlockRect.contains(x, y)) {
			BlockState targetBlock = CommonProxy.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.AIR.defaultBlockState(), 2000);
			return BlockPredicateHelper.getTooltips(targetBlock, recipe.getBlock());
		}
		return super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}

	@Override
	public boolean handleInput(DripstoneRecipe recipe, double mouseX, double mouseY, Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			int x = (int) mouseX;
			int y = (int) mouseY;
			if (sourceBlockRect.contains(x, y)) {
				BlockState fallingBlock = CommonProxy.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getSourceBlock()), Blocks.AIR.defaultBlockState(), 2000);
				return clickBlock(fallingBlock, input);
			}
			if (targetBlockRect.contains(x, y)) {
				BlockState landingBlock = CommonProxy.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.AIR.defaultBlockState(), 2000);
				return clickBlock(landingBlock, input);
			}
		}
		return super.handleInput(recipe, mouseX, mouseY, input);
	}

}
