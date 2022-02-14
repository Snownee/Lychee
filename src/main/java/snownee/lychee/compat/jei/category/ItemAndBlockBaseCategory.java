package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.jei.JEICompat;
import snownee.lychee.compat.jei.JEICompat.ScreenElementWrapper;
import snownee.lychee.compat.jei.SideBlockIcon;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.Pair;

public abstract class ItemAndBlockBaseCategory<C extends LycheeContext, T extends LycheeRecipe<C>> extends BaseJEICategory<C, T> {

	public static final Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public static final Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	public ItemAndBlockBaseCategory(List<LycheeRecipeType<C, T>> recipeTypes, IGuiHelper guiHelper, ScreenElement mainIcon) {
		super(recipeTypes, guiHelper);
		icon = new ScreenElementWrapper(new SideBlockIcon(mainIcon, this::getIconBlock));
		infoRect = new Rect2i(8, 32, 8, 8);
	}

	public BlockState getIconBlock() {
		ClientPacketListener con = Minecraft.getInstance().getConnection();
		if (con == null) {
			return Blocks.AIR.defaultBlockState();
		}
		/* off */
		return recipeTypes.stream()
				.map($ -> ((BlockKeyRecipeType<?, ?>) $).getMostUsedBlock())
				.max((a, b) -> a.getSecond() - b.getSecond())
				.map(Pair::getFirst)
				.orElse(Blocks.AIR.defaultBlockState());
		/* on */
	}

	@Nullable
	public BlockPredicate getInputBlock(T recipe) {
		return ((BlockKeyRecipe) recipe).getBlock();
	}

	public BlockState getRenderingBlock(T recipe) {
		return LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(getInputBlock(recipe)), Blocks.AIR.defaultBlockState(), 1000);
	}

	@Override
	public void setRecipe(IRecipeLayout layout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStackGroup = layout.getItemStacks();
		List<List<ItemStack>> items = ingredients.getInputs(VanillaTypes.ITEM);
		if (!items.isEmpty()) {
			itemStackGroup.init(0, true, 3, 12);
			itemStackGroup.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
			boolean preventDefault = recipe.getPostActions().stream().anyMatch($ -> $.getType() == PostActionTypes.PREVENT_DEFAULT);
			if (preventDefault) {
				itemStackGroup.setBackground(0, JEICompat.el(AllGuiTextures.JEI_CATALYST_SLOT));
				itemStackGroup.addTooltipCallback((i, input, stack, tooltip) -> {
					tooltip.add(recipe.getType().getPreventDefaultDescription(recipe));
				});
			} else {
				itemStackGroup.setBackground(0, JEICompat.slot(false));
			}
		}

		actionGroup(layout, recipe, 88, recipe.getShowingPostActions().size() > 9 ? 26 : 28);
	}

	public void drawExtra(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 26, 18);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void draw(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		super.draw(recipe, matrixStack, mouseX, mouseY);
		drawExtra(recipe, matrixStack, mouseX, mouseY);

		BlockState state = getRenderingBlock(recipe);
		if (state.isAir()) {
			AllGuiTextures.JEI_QUESTION_MARK.render(matrixStack, getBlockRenderPosX() + 4, getBlockRenderPosY() + 2);
			return;
		}
		if (state.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(getBlockRenderPosX() + 11, getBlockRenderPosY() + 18, 0);
			matrixStack.scale(.7F, .7F, .7F);
			AllGuiTextures.JEI_SHADOW.render(matrixStack, -26, -5);
			matrixStack.popPose();
		}

		matrixStack.pushPose();
		matrixStack.mulPose(Vector3f.XP.rotationDegrees(-12.5f));
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
		matrixStack.translate(getBlockRenderPosX() - 9, getBlockRenderPosY() + 13, 0);
		GuiGameElement.of(state).scale(15).atLocal(0, 0, 2).render(matrixStack);
		matrixStack.popPose();
	}

	protected int getBlockRenderPosX() {
		return 30;
	}

	protected int getBlockRenderPosY() {
		return 35;
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
		inputBlockRect.setPosition(getBlockRenderPosX(), getBlockRenderPosY());
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
		inputBlockRect.setPosition(getBlockRenderPosX(), getBlockRenderPosY());
		if (getClass() != ItemBurningRecipeCategory.class && inputBlockRect.contains((int) mouseX, (int) mouseY)) {
			return clickBlock(getRenderingBlock(recipe), input);
		}
		return false;
	}

}
