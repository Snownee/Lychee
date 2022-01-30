package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.widget.QueuedTooltip.TooltipEntryImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.lychee.compat.rei.LEntryWidget;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class BaseREICategory<C extends LycheeContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<C, T>> implements DisplayCategory<D> {

	protected final List<LycheeRecipeType<C, T>> recipeTypes;
	protected Renderer bg;
	protected Renderer icon;

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

	//	@Override
	//	public abstract void setIngredients(T recipe, IIngredients ingredients);
	//
	//	@Override
	//	public abstract void setRecipe(IRecipeLayout layout, T recipe, IIngredients ingredients);

	public static LEntryWidget actionSlot(Point startPoint, PostAction action, int x, int y) {
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
		return slot;
	}
}
