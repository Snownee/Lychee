package snownee.lychee.compat.rei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.REICompat.ScreenElementWrapper;
import snownee.lychee.compat.rei.ReactiveWidget;
import snownee.lychee.compat.rei.SideBlockIcon;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.util.CommonProxy;

public abstract class ItemAndBlockBaseCategory<C extends LycheeContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<T>> extends LycheeCategory<C, T, D> {

	private final ScreenElement mainIcon;
	public Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	public ItemAndBlockBaseCategory(List<LycheeRecipeType<C, T>> recipeTypes, ScreenElement mainIcon) {
		super(recipeTypes);
		this.mainIcon = mainIcon;
		infoRect.setPosition(8, 32);
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
		return CommonProxy.getCycledItem(
				BlockPredicateHelper.getShowcaseBlockStates(getInputBlock(recipe)),
				Blocks.AIR.defaultBlockState(),
				1000);
	}

	public void drawExtra(T recipe, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, methodRect.getX(), methodRect.getY());
	}

	@Nullable
	public Component getMethodDescription(T recipe) {
		return null;
	}

	@Override
	public List<Widget> setupDisplay(D display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		T recipe = display.recipe;
		List<Widget> widgets = super.setupDisplay(display, bounds);
		drawInfoBadgeIfNeeded(widgets, display, startPoint);
		widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			PoseStack matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x, startPoint.y, 0);
			drawExtra(recipe, graphics, mouseX, mouseY, bounds.getCenterX());

			BlockState state = getRenderingBlock(recipe);
			if (state.isAir()) {
				AllGuiTextures.JEI_QUESTION_MARK.render(graphics, inputBlockRect.getX() + 4, inputBlockRect.getY() + 2);
				matrixStack.popPose();
				return;
			}
			if (state.getLightEmission() < 5) {
				matrixStack.pushPose();
				matrixStack.translate(inputBlockRect.getX() + 11, inputBlockRect.getY() + 16, 0);
				matrixStack.scale(.7F, .7F, .7F);
				AllGuiTextures.JEI_SHADOW.render(graphics, -26, -5);
				matrixStack.popPose();
			}

			/* off */
			GuiGameElement.of(state)
					.rotateBlock(12.5, -22.5, 0)
					.scale(15)
					.lighting(JEIREI.BLOCK_LIGHTING)
					.atLocal(0, 0.2, 0)
					.at(inputBlockRect.getX(), inputBlockRect.getY())
					.render(graphics);
			/* on */
			matrixStack.popPose();
		}));

		int y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		if (recipe instanceof ItemShapelessRecipe) {
			ingredientGroup(widgets, startPoint, recipe, 40, y);
		} else if (recipe instanceof BlockInteractingRecipe) {
			ingredientGroup(widgets, startPoint, recipe, 22, 21);
		} else {
			ingredientGroup(widgets, startPoint, recipe, 12, 21);
		}

		actionGroup(widgets, startPoint, recipe, contentWidth() - 34, y);

		ReactiveWidget reactive;
		Component description = getMethodDescription(recipe);
		if (description != null) {
			reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, methodRect));
			reactive.setTooltipFunction($ -> {
				return new Component[]{description};
			});
			widgets.add(reactive);
		}

		if (recipeTypes.get(0) != RecipeTypes.ITEM_BURNING) {
			reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, inputBlockRect));
			reactive.setTooltipFunction($ -> {
				List<Component> list = BlockPredicateHelper.getTooltips(getRenderingBlock(recipe), getInputBlock(recipe));
				return list.toArray(new Component[0]);
			});
			reactive.setOnClick(($, button) -> {
				clickBlock(getRenderingBlock(recipe), button);
			});
			widgets.add(reactive);
		}

		return widgets;
	}

	@Override
	public Renderer createIcon(List<T> recipes) {
		return new ScreenElementWrapper(new SideBlockIcon(mainIcon, Suppliers.memoize(() -> getIconBlock(recipes))));
	}

}
