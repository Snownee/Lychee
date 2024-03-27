package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.jei.JEICompat.ScreenElementWrapper;
import snownee.lychee.compat.jei.SideBlockIcon;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.util.CommonProxy;

public abstract class ItemAndBlockBaseCategory<C extends LycheeContext, T extends LycheeRecipe<C>> extends BaseJEICategory<C, T> {

	public Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	private final ScreenElement mainIcon;

	public ItemAndBlockBaseCategory(List<LycheeRecipeType<C, T>> recipeTypes, ScreenElement mainIcon) {
		super(recipeTypes);
		this.mainIcon = mainIcon;
		infoRect.setPosition(8, 32);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper, List<T> recipes) {
		return new ScreenElementWrapper(new SideBlockIcon(mainIcon, Suppliers.memoize(() -> getIconBlock(recipes))));
	}

	public BlockState getIconBlock(List<T> recipes) {
		ClientPacketListener con = Minecraft.getInstance().getConnection();
		if (con == null) {
			return Blocks.AIR.defaultBlockState();
		}
		return JEIREI.getMostUsedBlock(recipes).getFirst();
	}

	@Nullable
	public BlockPredicate getInputBlock(T recipe) {
		return ((BlockKeyRecipe<?>) recipe).getBlock();
	}

	public BlockState getRenderingBlock(T recipe) {
		return CommonProxy.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(getInputBlock(recipe)), Blocks.AIR.defaultBlockState(), 1000);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		int y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		if (recipe instanceof ItemShapelessRecipe) {
			ingredientGroup(builder, recipe, 38, y);
		} else if (recipe instanceof BlockInteractingRecipe) {
			ingredientGroup(builder, recipe, 22, 21);
		} else {
			ingredientGroup(builder, recipe, 12, 21);
		}
		actionGroup(builder, recipe, getWidth() - 29, y);
		addBlockIngredients(builder, recipe);
	}

	public void drawExtra(T recipe, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, methodRect.getX(), methodRect.getY());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		drawInfoBadgeIfNeeded(recipe, graphics, mouseX, mouseY);
		int centerX = getWidth() / 2;
		drawExtra(recipe, graphics, mouseX, mouseY, centerX);

		BlockState state = getRenderingBlock(recipe);
		if (state.isAir()) {
			AllGuiTextures.JEI_QUESTION_MARK.render(graphics, inputBlockRect.getX() + 4, inputBlockRect.getY() + 2);
			return;
		}
		PoseStack matrixStack = graphics.pose();
		if (state.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(inputBlockRect.getX() + 11, inputBlockRect.getY() + 16, 0);
			matrixStack.scale(.7F, .7F, .7F);
			AllGuiTextures.JEI_SHADOW.render(graphics, -26, -5);
			matrixStack.popPose();
		}

		/* off */
		GuiGameElement.of(state)
				.rotateBlock(12.5, 202.5, 0)
				.scale(15)
				.lighting(JEIREI.BLOCK_LIGHTING)
				.atLocal(0, 0.2, 0)
				.at(inputBlockRect.getX(), inputBlockRect.getY())
				.render(graphics);
		/* on */
	}

	@Override
	public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (getClass() != ItemBurningRecipeCategory.class && inputBlockRect.contains((int) mouseX, (int) mouseY)) {
			return BlockPredicateHelper.getTooltips(getRenderingBlock(recipe), getInputBlock(recipe));
		}
		if (methodRect.contains((int) mouseX, (int) mouseY)) {
			Component description = getMethodDescription(recipe);
			if (description != null) {
				return List.of(description);
			}
		}
		return super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}

	@Nullable
	public Component getMethodDescription(T recipe) {
		return null;
	}

	@Override
	public boolean handleInput(T recipe, double mouseX, double mouseY, Key input) {
		if (getClass() != ItemBurningRecipeCategory.class && inputBlockRect.contains((int) mouseX, (int) mouseY)) {
			return clickBlock(getRenderingBlock(recipe), input);
		}
		return super.handleInput(recipe, mouseX, mouseY, input);
	}

}
