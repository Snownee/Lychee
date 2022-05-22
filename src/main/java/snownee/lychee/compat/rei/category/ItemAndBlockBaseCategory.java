package snownee.lychee.compat.rei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.LEntryWidget;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.REICompat.ScreenElementWrapper;
import snownee.lychee.compat.rei.ReactiveWidget;
import snownee.lychee.compat.rei.SideBlockIcon;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.Pair;

public abstract class ItemAndBlockBaseCategory<C extends LycheeContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<C, T>> extends BaseREICategory<C, T, D> {

	public Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	public ItemAndBlockBaseCategory(List<LycheeRecipeType<C, T>> recipeTypes, ScreenElement mainIcon) {
		super(recipeTypes);
		icon = new ScreenElementWrapper(new SideBlockIcon(mainIcon, Suppliers.memoize(this::getIconBlock)));
		infoRect = new Rect2i(23, 32, 8, 8);
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
		return ((BlockKeyRecipe<?>) recipe).getBlock();
	}

	public BlockState getRenderingBlock(T recipe) {
		return LUtil.getCycledItem(BlockPredicateHelper.getShowcaseBlockStates(getInputBlock(recipe)), Blocks.AIR.defaultBlockState(), 1000);
	}

	public void drawExtra(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 26, 18);
	}

	@Nullable
	public Component getMethodDescription(T recipe) {
		return null;
	}

	@Override
	public List<Widget> setupDisplay(D display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - getDisplayWidth(display) / 2 + 15, bounds.getY() + 4);
		T recipe = display.recipe;
		List<Widget> widgets = super.setupDisplay(display, bounds);
		widgets.add(Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x + inputBlockRect.getX() - 30, startPoint.y + inputBlockRect.getY() - 35, 0);
			drawExtra(recipe, matrixStack, mouseX, mouseY);

			BlockState state = getRenderingBlock(recipe);
			if (state.isAir()) {
				AllGuiTextures.JEI_QUESTION_MARK.render(matrixStack, 34, 37);
				matrixStack.popPose();
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
			matrixStack.popPose();
		}));

		List<EntryIngredient> items = display.getInputEntries();
		if (getClass() != BlockExplodingRecipeCategory.class && !items.isEmpty()) {
			boolean preventDefault = recipe.getPostActions().stream().anyMatch($ -> $.getType() == PostActionTypes.PREVENT_DEFAULT);
			LEntryWidget slot = REICompat.slot(startPoint, 3, 12, false, preventDefault);
			slot.entries(items.get(0));
			slot.markInput();
			if (preventDefault) {
				slot.addTooltipCallback(tooltip -> {
					tooltip.add(recipe.getType().getPreventDefaultDescription(recipe));
				});
			}
			widgets.add(slot);
		}

		actionGroup(widgets, startPoint, recipe, 88, recipe.getShowingPostActions().size() > 9 ? 26 : 28);

		ReactiveWidget reactive;
		Component description = getMethodDescription(recipe);
		if (description != null) {
			reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, methodRect));
			reactive.setTooltipFunction($ -> {
				return new Component[] { description };
			});
			widgets.add(reactive);
		}

		if (getClass() != ItemBurningRecipeCategory.class) {
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

}
