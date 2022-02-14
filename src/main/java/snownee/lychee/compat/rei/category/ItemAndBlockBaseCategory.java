package snownee.lychee.compat.rei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
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
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class ItemAndBlockBaseCategory<C extends LycheeContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<C, T>> extends BaseREICategory<C, T, D> {

	public static final int width = 116;
	public static final int height = 54;
	public static final Rect2i infoRect = new Rect2i(8, 32, 8, 8);
	public static final Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public static final Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	public ItemAndBlockBaseCategory(List<LycheeRecipeType<C, T>> recipeTypes, ScreenElement mainIcon) {
		super(recipeTypes);
		icon = new ScreenElementWrapper(new SideBlockIcon(mainIcon, this::getIconBlock));
	}

	@Override
	public int getDisplayHeight() {
		return height + 16;
	}

	public abstract BlockState getIconBlock();

	@Nullable
	public abstract BlockPredicate getInputBlock(T recipe);

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
		Point startPoint = new Point(bounds.getCenterX() - width / 2, bounds.getY() + 8);
		T recipe = display.recipe;
		List<Widget> widgets = Lists.newArrayList(super.setupDisplay(display, bounds));
		widgets.add(Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x, startPoint.y, 0);
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

		boolean preventDefault = recipe.getPostActions().stream().anyMatch($ -> $.getType() == PostActionTypes.PREVENT_DEFAULT);
		LEntryWidget slot = REICompat.slot(startPoint, 3, 12, false, preventDefault);
		slot.entries(display.getInputEntries().get(0));
		slot.markInput();
		if (preventDefault) {
			slot.addTooltipCallback(tooltip -> {
				tooltip.add(recipe.getType().getPreventDefaultDescription(recipe));
			});
		}
		widgets.add(slot);

		List<PostAction> actions = recipe.getShowingPostActions();
		if (!actions.isEmpty()) {
			int size = Math.min(actions.size(), 9);
			int gridX = (int) Math.ceil(Math.sqrt(size));
			int gridY = (int) Math.ceil((float) size / gridX);

			int x = 88 - gridX * 9, y = 26 - gridY * 9;
			for (int i = 0; i < gridY; i++) {
				for (int j = 0; j < gridX; j++) {
					int index = i * 3 + j;
					if (index >= size) {
						break;
					}
					widgets.add(actionSlot(startPoint, actions.get(index), x + j * 19, y + i * 19));
				}
			}
		}

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
				BlockState state = getRenderingBlock(recipe);
				ItemStack stack = new ItemStack(state.getBlock());
				if (stack.isEmpty()) {
					return;
				}
				ViewSearchBuilder searchBuilder = ViewSearchBuilder.builder();
				if (button == 0) {
					searchBuilder.addRecipesFor(EntryStacks.of(stack));
				} else if (button == 1) {
					searchBuilder.addUsagesFor(EntryStacks.of(stack));
				} else {
					return;
				}
				searchBuilder.open();
			});
			widgets.add(reactive);
		}

		if (!recipe.getConditions().isEmpty()) {
			widgets.add(Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
				matrixStack.pushPose();
				matrixStack.translate(startPoint.x + infoRect.getX(), startPoint.y + infoRect.getY(), 0);
				matrixStack.scale(.5F, .5F, .5F);
				AllGuiTextures.INFO.render(matrixStack, 0, 0);
				matrixStack.popPose();
			}));
			reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, infoRect));
			reactive.setTooltipFunction($ -> {
				List<Component> list = Lists.newArrayList();
				recipe.getConditonTooltips(list, 0);
				return list.toArray(new Component[0]);
			});
			widgets.add(reactive);
		}

		return widgets;
	}

}
