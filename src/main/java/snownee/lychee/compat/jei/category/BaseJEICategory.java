package snownee.lychee.compat.jei.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.jei.JEICompat;
import snownee.lychee.compat.jei.JEICompat.SlotType;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.RandomSelect;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class BaseJEICategory<C extends LycheeContext, T extends LycheeRecipe<C>> implements IRecipeCategory<T> {

	public static final int width = 119;
	public static final int height = 59;
	public final List<LycheeRecipeType<C, T>> recipeTypes;
	protected IDrawable bg;
	public IDrawable icon;
	protected Rect2i infoRect;
	public List<T> initialRecipes;
	public RecipeType<T> recipeType;

	public BaseJEICategory(LycheeRecipeType<C, T> recipeType) {
		this(List.of(recipeType));
	}

	public BaseJEICategory(List<LycheeRecipeType<C, T>> recipeTypes) {
		this.recipeTypes = recipeTypes;
		infoRect = new Rect2i(0, 25, 8, 8);
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
		return icon;
	}

	public abstract IDrawable createIcon(IGuiHelper guiHelper, List<T> recipes);

	@Override
	public Component getTitle() {
		return JEIREI.makeTitle(recipeType.getUid());
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
		var ingredients = JEIREI.generateInputs(recipe);
		slotGroup(builder, x + 1, y + 1, 0, ingredients, (layout0, ingredient, i, x0, y0) -> {
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, x0, y0);
			slot.addItemStacks(Stream.of(ingredient.left.getItems()).map($ -> ingredient.right == 1 ? $ : $.copy()).peek($ -> $.setCount(ingredient.right)).toList());
			slot.setBackground(JEICompat.slot(ingredient.middle != null ? SlotType.CATALYST : SlotType.NORMAL), -1, -1);
			if (ingredient.middle != null && ingredient.middle != LUtil.EMPTY_TEXT) {
				slot.addTooltipCallback((stack, tooltip) -> {
					tooltip.add(recipe.getType().getPreventDefaultDescription(recipe));
				});
			}
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
		Map<ItemStack, PostAction> itemMap = Maps.newIdentityHashMap();
		buildActionSlot(slot, action, itemMap);
		slot.addTooltipCallback((view, tooltip) -> {
			var optional = view.getDisplayedIngredient();
			if (optional.isEmpty()) {
				return;
			}
			var ingr = optional.get();
			var raw = ingr.getIngredient();
			if (!itemMap.containsKey(raw)) {
				return;
			}
			tooltip.clear();
			if (raw instanceof ItemStack) {

			}
			raw = itemMap.get(raw);
			List<Component> list;
			if (action instanceof RandomSelect) {
				list = ((RandomSelect) action).getTooltips((PostAction) raw);
			} else {
				list = action.getTooltips();
			}
			tooltip.addAll(list);
		});
		slot.setBackground(JEICompat.slot(action.getConditions().isEmpty() ? SlotType.NORMAL : SlotType.CHANCE), -1, -1);
	}

	private static void buildActionSlot(IRecipeSlotBuilder slot, PostAction action, Map<ItemStack, PostAction> itemMap) {
		if (action instanceof DropItem dropitem) {
			slot.addItemStack(dropitem.stack);
			itemMap.put(dropitem.stack, dropitem);
		} else if (action instanceof RandomSelect random) {
			for (PostAction entry : random.entries) {
				buildActionSlot(slot, entry, itemMap);
			}
		} else {
			slot.addIngredient(JEICompat.POST_ACTION, action);
		}
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		drawInfoBadge(recipe, matrixStack, mouseX, mouseY);
	}

	public void drawInfoBadge(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		drawInfoBadge(recipe, matrixStack, mouseX, mouseY, infoRect);
	}

	public static void drawInfoBadge(ILycheeRecipe<?> recipe, PoseStack matrixStack, double mouseX, double mouseY, Rect2i rect) {
		if (!recipe.getContextualHolder().getConditions().isEmpty() || !Strings.isNullOrEmpty(recipe.getComment())) {
			matrixStack.pushPose();
			matrixStack.translate(rect.getX(), rect.getY(), 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(matrixStack, 0, 0);
			matrixStack.popPose();
		}
	}

	@Override
	public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return getTooltipStrings(recipe, mouseX, mouseY, infoRect);
	}

	public static List<Component> getTooltipStrings(ILycheeRecipe<?> recipe, double mouseX, double mouseY, Rect2i rect) {
		if (rect.contains((int) mouseX, (int) mouseY)) {
			List<Component> list = Lists.newArrayList();
			if (!Strings.isNullOrEmpty(recipe.getComment())) {
				String comment = recipe.getComment();
				if (I18n.exists(comment)) {
					comment = I18n.get(comment);
				}
				Splitter.on('\n').splitToStream(comment).map(Component::literal).forEach(list::add);
			}
			recipe.getContextualHolder().getConditonTooltips(list, 0);
			return list;
		}
		return List.of();
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
				gui.show(factory.createFocus(input.getValue() == 1 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack));
				return true;
			}
		}
		return false;
	}

}
