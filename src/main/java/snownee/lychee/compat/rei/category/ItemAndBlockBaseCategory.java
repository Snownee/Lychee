package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.LycheeREIPlugin;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.InteractiveWidget;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ItemAndBlockBaseCategory<T extends ILycheeRecipe<LycheeContext>> extends AbstractLycheeCategory<T> {

	private final LycheeRecipeType<T> recipeType;
	public Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	public ItemAndBlockBaseCategory(
			CategoryIdentifier<? extends LycheeDisplay<T>> id,
			Renderer icon,
			LycheeRecipeType<T> recipeType) {
		super(id, icon);
		this.recipeType = recipeType;
		infoRect.setPosition(8, 32);
	}

	public static BlockState getIconBlock(Collection<RecipeHolder<? extends BlockKeyableRecipe<?>>> recipes) {
		var con = Minecraft.getInstance().getConnection();
		if (con == null) {
			return Blocks.AIR.defaultBlockState();
		}
		return JEIREI.getMostUsedBlock(recipes).getFirst();
	}

	public BlockPredicate getInputBlock(T recipe) {
		return ((BlockKeyableRecipe<?>) recipe).blockPredicate();
	}

	public BlockState getRenderingBlock(T recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(getInputBlock(recipe)),
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
	public List<Widget> setupDisplay(LycheeDisplay<T> display, Rectangle bounds) {
		var startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		var recipe = display.recipe();
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));
		drawInfoBadgeIfNeeded(widgets, display, startPoint);
		widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			var matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x, startPoint.y, 0);
			drawExtra(recipe, graphics, mouseX, mouseY, bounds.getCenterX());

			var state = getRenderingBlock(recipe);
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

			GuiGameElement.of(state)
					.rotateBlock(12.5, -22.5, 0)
					.scale(15)
					.lighting(JEIREI.BLOCK_LIGHTING)
					.atLocal(0, 0.2, 0)
					.at(inputBlockRect.getX(), inputBlockRect.getY())
					.render(graphics);
			matrixStack.popPose();
		}));

		var y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;

		renderIngredientGroup(widgets, startPoint, recipe, y);

		actionGroup(widgets, startPoint, recipe, contentWidth() - 34, y);

		InteractiveWidget reactive;
		var description = getMethodDescription(recipe);
		if (description != null) {
			reactive = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, methodRect));
			reactive.setTooltipFunction($ -> List.of(description));
			widgets.add(reactive);
		}

		if (needRenderInputBlock(recipe)) {
			reactive = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, inputBlockRect));
			reactive.setTooltipFunction($ -> BlockPredicateExtensions.getTooltips(getRenderingBlock(recipe), getInputBlock(recipe)));
			reactive.setOnClick(($, button) -> clickBlock(getRenderingBlock(recipe), button));
			widgets.add(reactive);
		}

		return widgets;
	}

	protected boolean needRenderInputBlock(T recipe) {
		return !BlockPredicateExtensions.isAny(getInputBlock(recipe));
	}

	protected void renderIngredientGroup(List<Widget> widgets, Point startPoint, T recipe, int y) {
		ingredientGroup(widgets, startPoint, recipe, 12, 21);
	}

	@Override
	public LycheeRecipeType<? extends T> recipeType() {
		return recipeType;
	}
}
