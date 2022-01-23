package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.jei.JEICompat;
import snownee.lychee.compat.jei.JEICompat.ScreenElementWrapper;
import snownee.lychee.compat.jei.SideBlockIcon;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class ItemAndBlockBaseCategory<C extends LycheeContext, T extends LycheeRecipe<C>> extends BaseJEICategory<C, T> {

	public static final int width = 116;
	public static final int height = 54;
	public static final Rect2i infoRect = new Rect2i(8, 32, 8, 8);
	public static final Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public static final Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	public ItemAndBlockBaseCategory(List<LycheeRecipeType<C, T>> recipeTypes, IGuiHelper guiHelper, ScreenElement mainIcon) {
		super(recipeTypes, guiHelper);
		bg = guiHelper.createBlankDrawable(width, height);
		icon = new ScreenElementWrapper(new SideBlockIcon(mainIcon, this::getIconBlock));
	}

	public abstract BlockState getIconBlock();

	public abstract void setInputs(T recipe, IIngredients ingredients);

	@Nullable
	public abstract BlockPredicate getInputBlock(T recipe);

	public BlockState getRenderingBlock(T recipe) {
		return LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(getInputBlock(recipe)), Blocks.AIR.defaultBlockState());
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		setInputs(recipe, ingredients);
		List<List<ItemStack>> outputs = recipe.getPostActions().stream().map(PostAction::getOutputItems).toList();
		ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
		ingredients.setOutputs(JEICompat.POST_ACTION, recipe.getPostActions());
	}

	@Override
	public void setRecipe(IRecipeLayout layout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStackGroup = layout.getItemStacks();
		itemStackGroup.init(0, true, 3, 12);
		itemStackGroup.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		itemStackGroup.setBackground(0, JEICompat.slot(false));

		int size = Math.min(recipe.getPostActions().size(), 9);
		int gridX = (int) Math.ceil(Math.sqrt(size));
		int gridY = (int) Math.ceil((float) size / gridX);

		int x = 88 - gridX * 9, y = 26 - gridY * 9;
		for (int i = 0; i < gridY; i++) {
			for (int j = 0; j < gridX; j++) {
				int index = i * 3 + j;
				if (index >= size) {
					break;
				}
				actionSlot(layout, recipe.getPostActions().get(index), index + 10, x + j * 19, y + i * 19);
			}
		}
	}

	public void drawExtra(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 26, 18);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void draw(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		super.draw(recipe, matrixStack, mouseX, mouseY);
		drawExtra(recipe, matrixStack, mouseX, mouseY);

		if (!recipe.getConditions().isEmpty()) {
			matrixStack.pushPose();
			matrixStack.translate(infoRect.getX(), infoRect.getY(), 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(matrixStack, 0, 0);
			matrixStack.popPose();
		}

		BlockState state = getRenderingBlock(recipe);
		if (state.isAir()) {
			AllGuiTextures.JEI_QUESTION_MARK.render(matrixStack, 23, 48);
			return;
		}
		if (state.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(23, 48, 0);
			matrixStack.scale(.7F, .7F, .7F);
			AllGuiTextures.JEI_SHADOW.render(matrixStack, 0, 0);
			matrixStack.popPose();
		}

		matrixStack.pushPose();
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		matrixStack.translate(21, 48, 0);
		GuiGameElement.of(state).scale(15).atLocal(0, 0, 2).render(matrixStack);
		matrixStack.popPose();
	}

	/**
	 * Get the tooltip for whatever's under the mouse.
	 * Ingredient tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @return tooltip strings. If there is no tooltip at this position, return an empty list.
	 */
	@Override
	public List<Component> getTooltipStrings(T recipe, double mouseX, double mouseY) {
		if (infoRect.contains((int) mouseX, (int) mouseY)) {
			List<Component> list = Lists.newArrayList();
			recipe.getConditonTooltips(list, 0);
			return list;
		}
		if (getClass() != ItemBurningRecipeCategory.class && inputBlockRect.contains((int) mouseX, (int) mouseY)) {
			return BlockPredicateHelper.getTooltips(getRenderingBlock(recipe), getInputBlock(recipe));
		}
		if (methodRect.contains((int) mouseX, (int) mouseY)) {
			Component description = getMethodDescription(recipe);
			if (description != null) {
				return List.of(description);
			}
		}
		return super.getTooltipStrings(recipe, mouseX, mouseY);
	}

	@Nullable
	public Component getMethodDescription(T recipe) {
		return null;
	}

	@Override
	public boolean handleInput(T recipe, double mouseX, double mouseY, Key input) {
		if (input.getType() == InputConstants.Type.MOUSE && getClass() != ItemBurningRecipeCategory.class && inputBlockRect.contains((int) mouseX, (int) mouseY)) {
			BlockState state = getRenderingBlock(recipe);
			ItemStack stack = new ItemStack(state.getBlock());
			if (!stack.isEmpty()) {
				IFocus<ItemStack> focus = JEICompat.RUNTIME.getRecipeManager().createFocus(input.getValue() == 1 ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, stack);
				JEICompat.RUNTIME.getRecipesGui().show(focus);
				return true;
			}
		}
		return false;
	}

}
