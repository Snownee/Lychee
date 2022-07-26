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
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.block_crushing.BlockCrushingContext;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public class BlockCrushingRecipeCategory extends BaseJEICategory<BlockCrushingContext, BlockCrushingRecipe> {

	public static final Rect2i fallingBlockRect = new Rect2i(5, -35, 20, 35);
	public static final Rect2i landingBlockRect = new Rect2i(5, 0, 20, 20);

	public BlockCrushingRecipeCategory(LycheeRecipeType<BlockCrushingContext, BlockCrushingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public int getWidth() {
		return width + 50;
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.ANVIL.getDefaultInstance());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BlockCrushingRecipe recipe, IFocusGroup focuses) {
		int xCenter = getWidth() / 2;
		int y = recipe.getIngredients().size() > 9 || recipe.getShowingPostActions().size() > 9 ? 26 : 28;
		ingredientGroup(builder, recipe, xCenter - 45, y, false);
		actionGroup(builder, recipe, xCenter + 50, y);
		addBlockInputs(builder, recipe.getBlock());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void draw(BlockCrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		super.draw(recipe, recipeSlotsView, matrixStack, mouseX, mouseY);
		BlockState fallingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.ANVIL.defaultBlockState(), 2000);
		BlockState landingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getLandingBlock()), Blocks.AIR.defaultBlockState(), 2000);
		int x = recipe.getIngredients().isEmpty() ? 36 : 72;
		boolean anyLandingBlock = recipe.getLandingBlock() == BlockPredicate.ANY;
		int y = anyLandingBlock ? 45 : 33;

		float ticks = (System.currentTimeMillis() % 2000) / 1000F;
		ticks = Math.min(1, ticks);
		ticks = ticks * ticks * ticks * ticks;

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
		GuiGameElement.of(fallingBlock).scale(15).atLocal(0, ticks * 1.3 - 1.3, 2).rotateBlock(0, 45, 0).render(matrixStack);
		if (!landingBlock.isAir()) {
			GuiGameElement.of(landingBlock).scale(15).atLocal(0, 1, 2).rotateBlock(0, 45, 0).render(matrixStack);
		}
		matrixStack.popPose();
	}

	@Override
	public List<Component> getTooltipStrings(BlockCrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		int x = recipe.getIngredients().isEmpty() ? 36 : 72;
		int y = recipe.getLandingBlock() == BlockPredicate.ANY ? 45 : 33;
		x = (int) mouseX - x;
		y = (int) mouseY - y;
		if (fallingBlockRect.contains(x, y)) {
			BlockState fallingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.ANVIL.defaultBlockState(), 2000);
			return BlockPredicateHelper.getTooltips(fallingBlock, recipe.getBlock());
		}
		if (recipe.getLandingBlock() != BlockPredicate.ANY && landingBlockRect.contains(x, y)) {
			BlockState landingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getLandingBlock()), Blocks.AIR.defaultBlockState(), 2000);
			return BlockPredicateHelper.getTooltips(landingBlock, recipe.getLandingBlock());
		}
		return super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}

	@Override
	public boolean handleInput(BlockCrushingRecipe recipe, double mouseX, double mouseY, Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			int x = recipe.getIngredients().isEmpty() ? 36 : 72;
			int y = recipe.getLandingBlock() == BlockPredicate.ANY ? 45 : 33;
			x = (int) mouseX - x;
			y = (int) mouseY - y;
			if (fallingBlockRect.contains(x, y)) {
				BlockState fallingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getBlock()), Blocks.ANVIL.defaultBlockState(), 2000);
				return clickBlock(fallingBlock, input);
			}
			if (recipe.getLandingBlock() != BlockPredicate.ANY && landingBlockRect.contains(x, y)) {
				BlockState landingBlock = LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(recipe.getLandingBlock()), Blocks.AIR.defaultBlockState(), 2000);
				return clickBlock(landingBlock, input);
			}
		}
		return false;
	}

}
