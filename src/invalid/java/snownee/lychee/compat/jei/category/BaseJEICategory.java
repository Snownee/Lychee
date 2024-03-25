package snownee.lychee.compat.jei.category;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import snownee.lychee.client.core.post.PostActionRenderer;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.jei.JEICompat;
import snownee.lychee.compat.jei.JEICompat.SlotType;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.CompoundAction;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.RandomSelect;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.ClientProxy;

public abstract class BaseJEICategory<C extends LycheeContext, T extends LycheeRecipe<C>> implements IRecipeCategory<T> {

	public static final int width = 119;
	public static final int height = 59;
	public final List<LycheeRecipeType<C, T>> recipeTypes;
	public IDrawable icon;
	public List<T> initialRecipes;
	public RecipeType<T> recipeType;
	protected IDrawable bg;
	protected Rect2i infoRect;

	public BaseJEICategory(LycheeRecipeType<C, T> recipeType) {
		this(List.of(recipeType));
	}

	public BaseJEICategory(List<LycheeRecipeType<C, T>> recipeTypes) {
		this.recipeTypes = recipeTypes;
		infoRect = new Rect2i(0, 25, 8, 8);
	}

	public static void addBlockIngredients(IRecipeLayoutBuilder builder, LycheeRecipe<?> recipe) {
		addBlockIngredients(builder, recipe.getBlockInputs(), RecipeIngredientRole.INPUT);
		addBlockIngredients(builder, recipe.getBlockOutputs(), RecipeIngredientRole.OUTPUT);
	}

	public static void addBlockIngredients(IRecipeLayoutBuilder builder, Iterable<BlockPredicate> blocks, RecipeIngredientRole role) {
		for (BlockPredicate block : blocks) {
			List<ItemStack> items = BlockPredicateHelper.getMatchedItemStacks(block);
			Set<Fluid> fluids = BlockPredicateHelper.getMatchedFluids(block);
			if (!items.isEmpty() || !fluids.isEmpty()) {
				IIngredientAcceptor<?> acceptor = builder.addInvisibleIngredients(role);
				acceptor.addItemStacks(items);
				fluids.forEach(fluid -> acceptor.addFluidStack(fluid, JEICompat.HELPERS.getPlatformFluidHelper().bucketVolume()));
			}
		}
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

	public static void actionSlot(IRecipeLayoutBuilder builder, PostAction action, int index, int x, int y) {
		IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x + 1, y + 1);
		Map<ItemStack, PostAction> itemMap = Maps.newIdentityHashMap();
		buildActionSlot(builder, action, itemMap, slot);
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
//			if (raw instanceof ItemStack) {
//
//			}
			raw = itemMap.get(raw);
			List<Component> list;
			if (action instanceof RandomSelect randomSelect) {
				list = PostActionRenderer.getTooltipsFromRandom(randomSelect, (PostAction) raw);
			} else {
				list = PostActionRenderer.of(action).getTooltips(action);
			}
			tooltip.addAll(list);
		});
		slot.setBackground(JEICompat.slot(action.getConditions().isEmpty() ? SlotType.NORMAL : SlotType.CHANCE), -1, -1);
	}

	private static void buildActionSlot(IRecipeLayoutBuilder layout, PostAction action, Map<ItemStack, PostAction> itemMap, IRecipeSlotBuilder slot) {
		if (action instanceof DropItem dropitem) {
			slot.addItemStack(dropitem.stack);
			itemMap.put(dropitem.stack, dropitem);
		} else if (action instanceof CompoundAction compoundAction) {
			compoundAction.getChildActions().filter(Predicate.not(PostAction::isHidden)).forEach(child -> buildActionSlot(layout, child, itemMap, slot));
		} else {
			slot.addIngredient(JEICompat.POST_ACTION, action);
			List<ItemStack> itemOutputs = action.getItemOutputs();
			if (!itemOutputs.isEmpty()) {
				layout.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(itemOutputs);
			}
		}
	}

	public static void drawInfoBadgeIfNeeded(ILycheeRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, Rect2i rect) {
		if (hasInfoBadge(recipe)) {
			PoseStack matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(rect.getX(), rect.getY(), 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(graphics, 0, 0);
			matrixStack.popPose();
		}
	}

	public static boolean hasInfoBadge(ILycheeRecipe<?> recipe) {
		return !recipe.getContextualHolder().getConditions().isEmpty() || !Strings.isNullOrEmpty(recipe.getComment());
	}

	public static List<Component> getTooltipStrings(ILycheeRecipe<?> recipe, double mouseX, double mouseY, Rect2i rect) {
		if (rect.contains((int) mouseX, (int) mouseY)) {
			return JEIREI.getRecipeTooltip(recipe);
		}
		return List.of();
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

	@Override
	public abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

	public void actionGroup(IRecipeLayoutBuilder builder, T recipe, int x, int y) {
		slotGroup(builder, x, y, 10000, ILycheeRecipe.filterHidden(recipe.getAllActions()).toList(), BaseJEICategory::actionSlot);
	}

	public void ingredientGroup(IRecipeLayoutBuilder builder, T recipe, int x, int y) {
		var ingredients = JEIREI.generateShapelessInputs(recipe);
		slotGroup(builder, x + 1, y + 1, 0, ingredients, (layout0, ingredient, i, x0, y0) -> {
			ItemStack[] items = ingredient.ingredient.getItems();
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, x0, y0);
			slot.addItemStacks(Stream.of(items).map($ -> ingredient.count == 1 ? $ : $.copy()).peek($ -> $.setCount(ingredient.count)).toList());
			slot.setBackground(JEICompat.slot(ingredient.isCatalyst ? SlotType.CATALYST : SlotType.NORMAL), -1, -1);
			if (!ingredient.tooltips.isEmpty()) {
				slot.addTooltipCallback((stack, tooltip) -> {
					tooltip.addAll(ingredient.tooltips);
				});
			}
		});
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		drawInfoBadgeIfNeeded(recipe, graphics, mouseX, mouseY);
	}

	public void drawInfoBadgeIfNeeded(T recipe, GuiGraphics graphics, double mouseX, double mouseY) {
		drawInfoBadgeIfNeeded(recipe, graphics, mouseX, mouseY, infoRect);
	}

	@Override
	public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return getTooltipStrings(recipe, mouseX, mouseY, infoRect);
	}

	@Override
	public boolean handleInput(T recipe, double mouseX, double mouseY, Key input) {
		if (input.getType() == InputConstants.Type.MOUSE && hasInfoBadge(recipe) && infoRect.contains((int) mouseX, (int) mouseY)) {
			return ClientProxy.postInfoBadgeClickEvent(recipe, input.getValue());
		}
		return false;
	}

	public boolean clickBlock(BlockState state, Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			if (state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
				state = Blocks.ANVIL.defaultBlockState();
			}
			IRecipesGui gui = JEICompat.RUNTIME.getRecipesGui();
			IFocusFactory factory = JEICompat.HELPERS.getFocusFactory();
			RecipeIngredientRole role = input.getValue() == 1 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
			ItemStack stack = state.getBlock().asItem().getDefaultInstance();
			if (!stack.isEmpty()) {
				gui.show(factory.createFocus(role, VanillaTypes.ITEM_STACK, stack));
				return true;
			} else if (state.getBlock() instanceof LiquidBlock) {
				IPlatformFluidHelper<IJeiFluidIngredient> fluidHelper = (IPlatformFluidHelper<IJeiFluidIngredient>) JEICompat.HELPERS.getPlatformFluidHelper();
				Fluid fluid = state.getFluidState().getType();
				gui.show(factory.createFocus(role, fluidHelper.getFluidIngredientType(), fluidHelper.create(fluid, fluidHelper.bucketVolume())));
				return true;
			}
		}
		return false;
	}

	@FunctionalInterface
	public interface SlotLayoutFunction<T> {
		void apply(IRecipeLayoutBuilder builder, T item, int index, int x, int y);
	}

}
