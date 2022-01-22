package snownee.lychee.compat.jei.category;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.compat.jei.JEICompat;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class BaseJEICategory<C extends LycheeContext, T extends LycheeRecipe<C>> implements IRecipeCategory<T> {

	protected final List<LycheeRecipeType<C, T>> recipeTypes;
	protected final IGuiHelper guiHelper;
	protected IDrawable bg;
	protected IDrawable icon;

	public BaseJEICategory(LycheeRecipeType<C, T> recipeType, IGuiHelper guiHelper) {
		this(List.of(recipeType), guiHelper);
	}

	public BaseJEICategory(List<LycheeRecipeType<C, T>> recipeTypes, IGuiHelper guiHelper) {
		this.recipeTypes = recipeTypes;
		this.guiHelper = guiHelper;
	}

	@Override
	public IDrawable getBackground() {
		return bg;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public Class<? extends T> getRecipeClass() {
		return recipeTypes.get(0).clazz;
	}

	@Override
	public Component getTitle() {
		return new TranslatableComponent(Util.makeDescriptionId("recipeType", getUid()));
	}

	@Override
	public ResourceLocation getUid() {
		return recipeTypes.get(0).id;
	}

	@Override
	public abstract void setIngredients(T recipe, IIngredients ingredients);

	@Override
	public abstract void setRecipe(IRecipeLayout layout, T recipe, IIngredients ingredients);

	@SuppressWarnings("rawtypes")
	public static void actionSlot(IRecipeLayout layout, PostAction action, int index, int x, int y) {
		IGuiIngredientGroup group;
		if (action instanceof DropItem) {
			group = layout.getItemStacks();
			group.init(index, false, x, y);
			if (!action.getConditions().isEmpty()) {
				group.addTooltipCallback((i, input, stack, tooltip) -> {
					if (i == index) {
						List<Component> list = Lists.newArrayList();
						list.add(LUtil.format("contextual.lychee", action.getConditions().size()).withStyle(ChatFormatting.GRAY));
						action.getConditonTooltips(list, 0);
						int line = Minecraft.getInstance().options.advancedItemTooltips ? 2 : 1;
						line = Math.min(tooltip.size(), line);
						tooltip.addAll(line, list);
					}
				});
			}
		} else {
			group = layout.getIngredientsGroup(JEICompat.POST_ACTION);
			group.init(index, false, PostActionIngredientRenderer.INSTANCE, x, y, 18, 18, 1, 1);
		}
		group.set(index, action instanceof DropItem ? ((DropItem) action).stack : action);
		group.setBackground(index, JEICompat.slot(!action.getConditions().isEmpty()));
	}
}
