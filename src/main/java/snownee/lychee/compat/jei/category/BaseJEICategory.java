package snownee.lychee.compat.jei.category;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.jei.JEICompat;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class BaseJEICategory<C extends LycheeContext, T extends LycheeRecipe<C>> implements IRecipeCategory<T> {

	public static final int width = 119;
	public static final int height = 59;
	protected final List<LycheeRecipeType<C, T>> recipeTypes;
	protected final IGuiHelper guiHelper;
	protected IDrawable bg;
	protected IDrawable icon;
	protected Rect2i infoRect;

	public BaseJEICategory(LycheeRecipeType<C, T> recipeType, IGuiHelper guiHelper) {
		this(List.of(recipeType), guiHelper);
	}

	public BaseJEICategory(List<LycheeRecipeType<C, T>> recipeTypes, IGuiHelper guiHelper) {
		this.recipeTypes = recipeTypes;
		this.guiHelper = guiHelper;
	}

	@Override
	public IDrawable getBackground() {
		if (bg == null) {
			bg = guiHelper.createBlankDrawable(getWidth(), getHeight());
		}
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

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		setInputs(recipe, ingredients);
		setOutputs(recipe, ingredients);
	}

	public void setInputs(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
	}

	public void setOutputs(T recipe, IIngredients ingredients) {
		List<PostAction> actions = recipe.getShowingPostActions();
		List<List<ItemStack>> outputs = actions.stream().map(PostAction::getOutputItems).toList();
		ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
		ingredients.setOutputs(JEICompat.POST_ACTION, actions);
	}

	@Override
	public abstract void setRecipe(IRecipeLayout layout, T recipe, IIngredients ingredients);

	public void actionGroup(IRecipeLayout layout, T recipe, int x, int y) {
		slotGroup(layout, x, y, 10000, recipe.getShowingPostActions(), BaseJEICategory::actionSlot);
	}

	public void ingredientGroup(IRecipeLayout layout, T recipe, int x, int y) {
		slotGroup(layout, x, y, 0, recipe.getIngredients(), (layout0, ingredient, i, x0, y0) -> {
			layout.getItemStacks().init(i, true, x0, y0);
			layout.getItemStacks().set(i, List.of(ingredient.getItems()));
			layout.getItemStacks().setBackground(i, JEICompat.slot(false));
		});
	}

	public static <T> void slotGroup(IRecipeLayout layout, int x, int y, int startIndex, List<T> items, SlotLayoutFunction<T> layoutFunction) {
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
				layoutFunction.apply(layout, items.get(index), startIndex + index, x + j * 19, y + i * 19);
				++index;
			}
		}
	}

	@FunctionalInterface
	public interface SlotLayoutFunction<T> {
		void apply(IRecipeLayout layout, T item, int index, int x, int y);
	}

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
						list.add(LUtil.format("contextual.lychee", action.showingConditionsCount()).withStyle(ChatFormatting.GRAY));
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

	@Override
	public void draw(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		if (!recipe.getConditions().isEmpty()) {
			matrixStack.pushPose();
			matrixStack.translate(infoRect.getX(), infoRect.getY(), 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(matrixStack, 0, 0);
			matrixStack.popPose();
		}
	}

	@Override
	public List<Component> getTooltipStrings(T recipe, double mouseX, double mouseY) {
		if (infoRect.contains((int) mouseX, (int) mouseY)) {
			List<Component> list = Lists.newArrayList();
			recipe.getConditonTooltips(list, 0);
			return list;
		}
		return IRecipeCategory.super.getTooltipStrings(recipe, mouseX, mouseY);
	}

	public boolean clickBlock(BlockState state, Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			if (state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
				state = Blocks.ANVIL.defaultBlockState();
			}
			ItemStack stack = state.getBlock().asItem().getDefaultInstance();
			if (!stack.isEmpty()) {
				IFocus<ItemStack> focus = JEICompat.RUNTIME.getRecipeManager().createFocus(input.getValue() == 1 ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, stack);
				JEICompat.RUNTIME.getRecipesGui().show(focus);
				return true;
			}
		}
		return false;
	}

}
