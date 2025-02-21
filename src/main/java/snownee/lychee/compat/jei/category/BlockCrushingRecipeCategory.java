package snownee.lychee.compat.jei.category;

import com.mojang.blaze3d.platform.InputConstants;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipeType;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

@NotNullByDefault
public final class BlockCrushingRecipeCategory extends AbstractLycheeCategory<BlockCrushingRecipe> {

	public static final Rect2i FALLING_BLOCK_RECT = new Rect2i(0, -35, 20, 35);
	public static final Rect2i LANDING_BLOCK_RECT = new Rect2i(0, 0, 20, 20);

	public BlockCrushingRecipeCategory(RecipeType<RecipeHolder<BlockCrushingRecipe>> recipeType, IDrawable icon) {
		super(recipeType, icon);
	}

	@Override
	public int contentWidth() {
		return WIDTH + 50;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BlockCrushingRecipe> recipeHolder, IFocusGroup focuses) {
		var recipe = recipeHolder.value();
		var xCenter = getWidth() / 2;
		var y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		ingredientGroup(builder, recipe, xCenter - 45, y);
		actionGroup(builder, recipe, xCenter + 50, y);
		LycheeCategory.addBlockIngredients(builder, recipe);
	}

	@Override
	public void draw(
			RecipeHolder<BlockCrushingRecipe> recipeHolder,
			IRecipeSlotsView recipeSlotsView,
			GuiGraphics guiGraphics,
			double mouseX,
			double mouseY
	) {
		var recipe = recipeHolder.value();
		drawInfoBadgeIfNeeded(guiGraphics, recipe, mouseX, mouseY);

		var fallingBlock = getFallingBlock(recipe);
		var landingBlock = getLandingBlock(recipe);

		var x = recipe.getIngredients().isEmpty() ? 41 : 77;
		var anyLandingBlock = BlockPredicateExtensions.isAny(recipe.landingBlock());
		var y = anyLandingBlock ? 45 : 36;

		var ticks = (System.currentTimeMillis() % 2000) / 1000F;
		ticks = Math.min(1, ticks);
		ticks = ticks * ticks * ticks * ticks;

		var matrixStack = guiGraphics.pose();
		if (landingBlock.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(x + 10.5, y + (anyLandingBlock ? 1 : 16), 0);
			var shadow = 0.6F;
			if (anyLandingBlock) {
				shadow = 0.2F + ticks * 0.2F;
			}
			matrixStack.scale(shadow, shadow, shadow);
			matrixStack.translate(-26, -5.5, 0);
			AllGuiTextures.JEI_SHADOW.render(guiGraphics, 0, 0);
			matrixStack.popPose();
		}

		matrixStack.pushPose();
		matrixStack.translate(x, y - 13, 0);
		GuiGameElement.of(fallingBlock)
				.scale(15)
				.atLocal(0, ticks * 1.3 - 1.3, 0)
				.rotateBlock(20, 225, 0)
				.lighting(JEIREI.BLOCK_LIGHTING)
				.at(0, 0, 300)
				.render(guiGraphics);
		if (!landingBlock.isAir()) {
			GuiGameElement.of(landingBlock).scale(15).atLocal(0, 1, 0).rotateBlock(20, 225, 0).lighting(JEIREI.BLOCK_LIGHTING).render(
					guiGraphics);
		}
		matrixStack.popPose();
	}

	@Override
	public void getTooltip(
			ITooltipBuilder tooltip,
			RecipeHolder<BlockCrushingRecipe> recipeHolder,
			IRecipeSlotsView recipeSlotsView,
			double mouseX,
			double mouseY) {
		var recipe = recipeHolder.value();
		var x = recipe.getIngredients().isEmpty() ? 41 : 77;
		var anyLandingBlock = BlockPredicateExtensions.isAny(recipe.landingBlock());
		var y = anyLandingBlock ? 45 : 36;
		x = (int) mouseX - x;
		y = (int) mouseY - y;
		if (FALLING_BLOCK_RECT.contains(x, y)) {
			var fallingBlock = CommonProxy.getCycledItem(
					BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
					Blocks.ANVIL.defaultBlockState(),
					2000);
			tooltip.addAll(BlockPredicateExtensions.getTooltips(fallingBlock, recipe.blockPredicate()));
		} else if (!anyLandingBlock && LANDING_BLOCK_RECT.contains(x, y)) {
			var landingBlock = CommonProxy.getCycledItem(
					BlockPredicateExtensions.getShowcaseBlockStates(recipe.landingBlock()),
					Blocks.AIR.defaultBlockState(),
					2000);
			tooltip.addAll(BlockPredicateExtensions.getTooltips(landingBlock, recipe.landingBlock()));
		}
	}

	@Override
	public boolean handleInput(RecipeHolder<BlockCrushingRecipe> recipeHolder, double mouseX, double mouseY, InputConstants.Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			var recipe = recipeHolder.value();
			var x = recipe.getIngredients().isEmpty() ? 41 : 77;
			var anyLandingBlock = BlockPredicateExtensions.isAny(recipe.landingBlock());
			var y = anyLandingBlock ? 45 : 36;
			x = (int) mouseX - x;
			y = (int) mouseY - y;
			if (FALLING_BLOCK_RECT.contains(x, y)) {
				BlockState fallingBlock = CommonProxy.getCycledItem(
						BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
						Blocks.ANVIL.defaultBlockState(),
						2000);
				return clickBlock(fallingBlock, input);
			}
			if (!anyLandingBlock && LANDING_BLOCK_RECT.contains(x, y)) {
				BlockState landingBlock = CommonProxy.getCycledItem(
						BlockPredicateExtensions.getShowcaseBlockStates(recipe.landingBlock()),
						Blocks.AIR.defaultBlockState(),
						2000);
				return clickBlock(landingBlock, input);
			}
		}
		return false;
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
}
