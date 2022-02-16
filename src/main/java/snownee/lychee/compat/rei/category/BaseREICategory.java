package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.widget.QueuedTooltip.TooltipEntryImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.rei.LEntryWidget;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.ReactiveWidget;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class BaseREICategory<C extends LycheeContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<C, T>> implements DisplayCategory<D> {

	public static final int width = 150;
	public static final int height = 59;
	protected final List<LycheeRecipeType<C, T>> recipeTypes;
	protected Renderer icon;
	protected Rect2i infoRect = new Rect2i(8, 32, 8, 8);

	public BaseREICategory(LycheeRecipeType<C, T> recipeType) {
		this(List.of(recipeType));
	}

	public BaseREICategory(List<LycheeRecipeType<C, T>> recipeTypes) {
		this.recipeTypes = recipeTypes;
	}

	@Override
	public Renderer getIcon() {
		return icon;
	}

	@Override
	public Component getTitle() {
		return new TranslatableComponent(Util.makeDescriptionId("recipeType", getIdentifier()));
	}

	@Override
	public int getDisplayHeight() {
		return height + 8;
	}

	@Override
	public int getDisplayWidth(D display) {
		return 150;
	}

	//	@Override
	//	public abstract void setIngredients(T recipe, IIngredients ingredients);
	//
	//	@Override
	//	public abstract void setRecipe(IRecipeLayout layout, T recipe, IIngredients ingredients);

	public void actionGroup(List<Widget> widgets, Point startPoint, T recipe, int x, int y) {
		slotGroup(widgets, startPoint, x, y, recipe.getShowingPostActions(), BaseREICategory::actionSlot);
	}

	public void ingredientGroup(List<Widget> widgets, Point startPoint, T recipe, int x, int y) {
		slotGroup(widgets, startPoint, x, y, recipe.getIngredients(), (widgets0, startPoint0, ingredient, x0, y0) -> {
			LEntryWidget slot = REICompat.slot(startPoint, x0, y0, false, false);
			slot.entries(EntryIngredients.ofIngredient(ingredient));
			slot.markInput();
			widgets.add(slot);
		});
	}

	public static <T> void slotGroup(List<Widget> widgets, Point startPoint, int x, int y, List<T> items, SlotLayoutFunction<T> layoutFunction) {
		int size = Math.min(items.size(), 9);
		int gridX = (int) Math.ceil(Math.sqrt(size));
		int gridY = (int) Math.ceil((float) size / gridX);

		x -= gridX * 9;
		y -= gridY * 9;

		int index = 0;
		for (int i = 0; i < gridY; i++) {
			for (int j = 0; j < gridX; j++) {
				if (index >= size) {
					break;
				}
				layoutFunction.apply(widgets, startPoint, items.get(index), x + j * 19, y + i * 19);
				++index;
			}
		}
	}

	@FunctionalInterface
	public interface SlotLayoutFunction<T> {
		void apply(List<Widget> widgets, Point startPoint, T item, int x, int y);
	}

	public static void actionSlot(List<Widget> widgets, Point startPoint, PostAction action, int x, int y) {
		LEntryWidget slot = REICompat.slot(startPoint, x, y, !action.getConditions().isEmpty(), false);
		slot.markOutput();
		if (action instanceof DropItem) {
			slot.entry(EntryStacks.of(((DropItem) action).stack));
			if (!action.getConditions().isEmpty()) {
				slot.addTooltipCallback(tooltip -> {
					List<Component> list = Lists.newArrayList();
					list.add(LUtil.format("contextual.lychee", action.showingConditionsCount()).withStyle(ChatFormatting.GRAY));
					action.getConditonTooltips(list, 0);
					int line = Minecraft.getInstance().options.advancedItemTooltips ? 2 : 1;
					line = Math.min(tooltip.entries().size(), line);
					tooltip.entries().addAll(line, list.stream().map(TooltipEntryImpl::new).toList());
				});
			}
		} else {
			slot.entry(EntryStack.of(REICompat.POST_ACTION, action));
		}
		widgets.add(slot);
	}

	@Override
	public List<Widget> setupDisplay(D display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - getDisplayWidth(display) / 2, bounds.getY() + 4);
		T recipe = display.recipe;
		List<Widget> widgets = Lists.newArrayList(DisplayCategory.super.setupDisplay(display, bounds));
		if (!recipe.getConditions().isEmpty()) {
			widgets.add(Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
				matrixStack.pushPose();
				matrixStack.translate(startPoint.x + infoRect.getX(), startPoint.y + infoRect.getY(), 0);
				matrixStack.scale(.5F, .5F, .5F);
				AllGuiTextures.INFO.render(matrixStack, 0, 0);
				matrixStack.popPose();
			}));
			ReactiveWidget reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, infoRect));
			reactive.setTooltipFunction($ -> {
				List<Component> list = Lists.newArrayList();
				recipe.getConditonTooltips(list, 0);
				return list.toArray(new Component[0]);
			});
			widgets.add(reactive);
		}
		return widgets;
	}

	public boolean clickBlock(BlockState state, int button) {
		if (state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
			state = Blocks.ANVIL.defaultBlockState();
		}
		ItemStack stack = state.getBlock().asItem().getDefaultInstance();
		if (stack.isEmpty()) {
			return false;
		}
		ViewSearchBuilder searchBuilder = ViewSearchBuilder.builder();
		if (button == 0) {
			searchBuilder.addRecipesFor(EntryStacks.of(stack));
		} else if (button == 1) {
			searchBuilder.addUsagesFor(EntryStacks.of(stack));
		} else {
			return false;
		}
		searchBuilder.open();
		return true;
	}
}
