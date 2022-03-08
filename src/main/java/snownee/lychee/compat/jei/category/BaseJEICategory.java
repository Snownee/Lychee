package snownee.lychee.compat.jei.category;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.BlockPredicate;
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
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class BaseJEICategory<C extends LycheeContext, T extends LycheeRecipe<C>> implements IRecipeCategory<T> {

	public static final int width = 119;
	public static final int height = 59;
	public final List<LycheeRecipeType<C, T>> recipeTypes;
	protected IDrawable bg;
	protected IDrawable icon;
	protected Rect2i infoRect;
	protected RecipeType<T> recipeType;

	public BaseJEICategory(LycheeRecipeType<C, T> recipeType) {
		this(List.of(recipeType));
	}

	public BaseJEICategory(List<LycheeRecipeType<C, T>> recipeTypes) {
		this.recipeTypes = recipeTypes;
		recipeType = new RecipeType<>(recipeTypes.get(0).id, recipeTypes.get(0).clazz);
	}

	@Override
	public Class<? extends T> getRecipeClass() {
		return recipeType.getRecipeClass();
	}

	@Override
	public ResourceLocation getUid() {
		return recipeType.getUid();
	}

	@Override
	public RecipeType<T> getRecipeType() {
		return recipeType;
	}

	@Override
	public ResourceLocation getRegistryName(T recipe) {
		return recipe.getId();
	}

	@Override
	public IDrawable getBackground() {
		if (bg == null) {
			bg = JEICompat.GUI.createBlankDrawable(getWidth(), getHeight());
		}
		return bg;
	}

	@Override
	public IDrawable getIcon() {
		if (icon == null) {
			icon = createIcon(JEICompat.GUI);
		}
		return icon;
	}

	public abstract IDrawable createIcon(IGuiHelper guiHelper);

	@Override
	public Component getTitle() {
		return new TranslatableComponent(Util.makeDescriptionId("recipeType", getUid()));
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public static void addBlockInputs(IRecipeLayoutBuilder builder, BlockPredicate block) {
		if (block == null)
			return;
		List<ItemStack> items = BlockPredicateHelper.getMatchedItemStacks(block);
		if (!items.isEmpty()) {
			IIngredientAcceptor<?> acceptor = builder.addInvisibleIngredients(RecipeIngredientRole.INPUT);
			acceptor.addItemStacks(items);
		}
	}

	@Override
	public abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

	public void actionGroup(IRecipeLayoutBuilder builder, T recipe, int x, int y) {
		slotGroup(builder, x, y, 10000, recipe.getShowingPostActions(), BaseJEICategory::actionSlot);
	}

	public void ingredientGroup(IRecipeLayoutBuilder builder, T recipe, int x, int y) {
		slotGroup(builder, x + 1, y + 1, 0, recipe.getIngredients(), (layout0, ingredient, i, x0, y0) -> {
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, x0, y0);
			slot.addIngredients(ingredient);
			slot.setBackground(JEICompat.slot(false), -1, -1);
		});
	}

	public static <T> void slotGroup(IRecipeLayoutBuilder builder, int x, int y, int startIndex, List<T> items, SlotLayoutFunction<T> layoutFunction) {
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
				layoutFunction.apply(builder, items.get(index), startIndex + index, x + j * 19, y + i * 19);
				++index;
			}
		}
	}

	@FunctionalInterface
	public interface SlotLayoutFunction<T> {
		void apply(IRecipeLayoutBuilder builder, T item, int index, int x, int y);
	}

	public static void actionSlot(IRecipeLayoutBuilder builder, PostAction action, int index, int x, int y) {
		IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x + 1, y + 1);
		if (action instanceof DropItem) {
			slot.addItemStack(((DropItem) action).stack);
			if (!action.getConditions().isEmpty()) {
				slot.addTooltipCallback((stack, tooltip) -> {
					List<Component> list = Lists.newArrayList();
					list.add(LUtil.format("contextual.lychee", action.showingConditionsCount()).withStyle(ChatFormatting.GRAY));
					action.getConditonTooltips(list, 0);
					int line = Minecraft.getInstance().options.advancedItemTooltips ? 2 : 1;
					line = Math.min(tooltip.size(), line);
					tooltip.addAll(line, list);
				});
			}
		} else {
			slot.addIngredient(JEICompat.POST_ACTION, action);
		}
		slot.setBackground(JEICompat.slot(!action.getConditions().isEmpty()), -1, -1);
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		if (!recipe.getConditions().isEmpty()) {
			matrixStack.pushPose();
			matrixStack.translate(infoRect.getX(), infoRect.getY(), 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(matrixStack, 0, 0);
			matrixStack.popPose();
		}
	}

	@Override
	public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (infoRect.contains((int) mouseX, (int) mouseY)) {
			List<Component> list = Lists.newArrayList();
			recipe.getConditonTooltips(list, 0);
			return list;
		}
		return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}

	public boolean clickBlock(BlockState state, Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			if (state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
				state = Blocks.ANVIL.defaultBlockState();
			}
			ItemStack stack = state.getBlock().asItem().getDefaultInstance();
			if (!stack.isEmpty()) {
				IRecipesGui gui = JEICompat.RUNTIME.getRecipesGui();
				IFocusFactory factory = JEICompat.HELPERS.getFocusFactory();
				gui.show(factory.createFocus(input.getValue() == 1 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM, stack));
				return true;
			}
		}
		return false;
	}

}
