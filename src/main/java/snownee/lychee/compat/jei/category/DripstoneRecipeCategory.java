package snownee.lychee.compat.jei.category;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.dripstone_dripping.DripstoneContext;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.util.LUtil;

public class DripstoneRecipeCategory extends BaseJEICategory<DripstoneContext, DripstoneRecipe> {

	private Rect2i sourceBlockRect = new Rect2i(23, 1, 16, 16);
	private Rect2i targetBlockRect = new Rect2i(23, 43, 16, 16);

	public DripstoneRecipeCategory(LycheeRecipeType<DripstoneContext, DripstoneRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.POINTED_DRIPSTONE.getDefaultInstance());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DripstoneRecipe recipe, IFocusGroup focuses) {
		int y = recipe.getShowingPostActions().size() > 9 ? 26 : 28;
		actionGroup(builder, recipe, getWidth() - 28, y);
		addBlockInputs(builder, recipe.getSourceBlock());
		addBlockInputs(builder, recipe.getBlock());
	}

	@Override
	public void draw(DripstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		drawInfoBadge(recipe, matrixStack, mouseX, mouseY);
		BlockState sourceBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getSourceBlock()), Blocks.AIR.defaultBlockState(), 2000);
		BlockState targetBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.AIR.defaultBlockState(), 2000);

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
		matrixStack.translate(0, 0, 300);
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		matrixStack.translate(15, 35, 0);
		GuiGameElement.of(sourceBlock).scale(12).atLocal(0, -2, 2).rotateBlock(0, 45, 0).render(matrixStack);
		GuiGameElement.of(Blocks.DRIPSTONE_BLOCK.defaultBlockState()).scale(12).atLocal(0, -1, 2).rotateBlock(0, 45, 0).render(matrixStack);
		GuiGameElement.of(Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)).scale(12).atLocal(0, 0, 2).rotateBlock(0, 45, 0).render(matrixStack);
		GuiGameElement.of(targetBlock).scale(12).atLocal(0, 1.5, 2).rotateBlock(0, 45, 0).render(matrixStack);
		matrixStack.popPose();
	}

	@Override
	public List<Component> getTooltipStrings(DripstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		int x = (int) mouseX;
		int y = (int) mouseY;
		if (sourceBlockRect.contains(x, y)) {
			BlockState sourceBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getSourceBlock()), Blocks.AIR.defaultBlockState(), 2000);
			return BlockPredicateHelper.getTooltips(sourceBlock, recipe.getSourceBlock());
		}
		if (targetBlockRect.contains(x, y)) {
			BlockState targetBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.AIR.defaultBlockState(), 2000);
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
				BlockState fallingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getSourceBlock()), Blocks.AIR.defaultBlockState(), 2000);
				return clickBlock(fallingBlock, input);
			}
			if (targetBlockRect.contains(x, y)) {
				BlockState landingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.AIR.defaultBlockState(), 2000);
				return clickBlock(landingBlock, input);
			}
		}
		return false;
	}

}
