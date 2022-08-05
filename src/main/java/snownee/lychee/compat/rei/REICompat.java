package snownee.lychee.compat.rei;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.category.BlockCrushingRecipeCategory;
import snownee.lychee.compat.rei.category.BlockExplodingRecipeCategory;
import snownee.lychee.compat.rei.category.BlockInteractionRecipeCategory;
import snownee.lychee.compat.rei.category.ItemBurningRecipeCategory;
import snownee.lychee.compat.rei.category.ItemExplodingRecipeCategory;
import snownee.lychee.compat.rei.category.ItemInsideRecipeCategory;
import snownee.lychee.compat.rei.category.ItemShapelessRecipeCategory;
import snownee.lychee.compat.rei.display.BlockCrushingDisplay;
import snownee.lychee.compat.rei.display.BlockExplodingDisplay;
import snownee.lychee.compat.rei.display.BlockInteractionDisplay;
import snownee.lychee.compat.rei.display.ItemBurningDisplay;
import snownee.lychee.compat.rei.display.ItemInsideDisplay;
import snownee.lychee.compat.rei.display.ItemShapelessDisplay;
import snownee.lychee.compat.rei.ingredient.PostActionIngredientHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public class REICompat implements REIClientPlugin {

	public static final ResourceLocation UID = new ResourceLocation(Lychee.ID, "main");
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(LycheeRegistries.POST_ACTION.key().location());

	public static final CategoryIdentifier<ItemBurningDisplay> ITEM_BURNING = CategoryIdentifier.of(RecipeTypes.ITEM_BURNING.id);
	public static final CategoryIdentifier<ItemInsideDisplay> ITEM_INSIDE = CategoryIdentifier.of(RecipeTypes.ITEM_INSIDE.id);
	public static final CategoryIdentifier<BlockInteractionDisplay> BLOCK_INTERACTION = CategoryIdentifier.of(RecipeTypes.BLOCK_INTERACTING.id);
	public static final CategoryIdentifier<BlockCrushingDisplay> BLOCK_CRUSHING = CategoryIdentifier.of(RecipeTypes.BLOCK_CRUSHING.id);
	@SuppressWarnings("rawtypes")
	public static final CategoryIdentifier<ItemShapelessDisplay> LIGHTNING_CHANNELING = CategoryIdentifier.of(RecipeTypes.LIGHTNING_CHANNELING.id);
	@SuppressWarnings("rawtypes")
	public static final CategoryIdentifier<ItemShapelessDisplay> ITEM_EXPLODING = CategoryIdentifier.of(RecipeTypes.ITEM_EXPLODING.id);
	public static final CategoryIdentifier<BlockExplodingDisplay> BLOCK_EXPLODING = CategoryIdentifier.of(RecipeTypes.BLOCK_EXPLODING.id);

	@SuppressWarnings("rawtypes")
	@Override
	public void registerCategories(CategoryRegistry registration) {
		registration.add(new ItemBurningRecipeCategory(RecipeTypes.ITEM_BURNING));
		registration.add(new ItemInsideRecipeCategory(RecipeTypes.ITEM_INSIDE, AllGuiTextures.JEI_DOWN_ARROW));
		ScreenElement mainIcon = RecipeTypes.BLOCK_INTERACTING.isEmpty() ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		registration.add(new BlockInteractionRecipeCategory((List) List.of(RecipeTypes.BLOCK_INTERACTING, RecipeTypes.BLOCK_CLICKING), mainIcon));
		registration.add(new BlockCrushingRecipeCategory(RecipeTypes.BLOCK_CRUSHING));
		registration.add(new ItemShapelessRecipeCategory<>(RecipeTypes.LIGHTNING_CHANNELING, EntryStacks.of(Items.LIGHTNING_ROD)));
		registration.add(new ItemExplodingRecipeCategory(RecipeTypes.ITEM_EXPLODING, EntryStacks.of(Items.TNT)));
		registration.add(new BlockExplodingRecipeCategory(RecipeTypes.BLOCK_EXPLODING, GuiGameElement.of(Items.TNT)));

		registration.removePlusButton(ITEM_BURNING);
		registration.removePlusButton(ITEM_INSIDE);
		registration.removePlusButton(BLOCK_INTERACTION);
		registration.removePlusButton(BLOCK_CRUSHING);
		registration.removePlusButton(LIGHTNING_CHANNELING);
		registration.removePlusButton(ITEM_EXPLODING);
		registration.removePlusButton(BLOCK_EXPLODING);

		for (ItemStack stack : RecipeTypes.BLOCK_CRUSHING.blockKeysToItems()) {
			registration.addWorkstations(BLOCK_CRUSHING, EntryStacks.of(stack));
		}
		registration.addWorkstations(LIGHTNING_CHANNELING, EntryStacks.of(Items.LIGHTNING_ROD));
		for (Item item : LUtil.tagElements(Registry.ITEM, LycheeTags.ITEM_EXPLODING_CATALYSTS)) {
			EntryStack<ItemStack> stack = EntryStacks.of(item);
			registration.addWorkstations(ITEM_EXPLODING, stack);
		}
		for (Item item : LUtil.tagElements(Registry.ITEM, LycheeTags.BLOCK_EXPLODING_CATALYSTS)) {
			EntryStack<ItemStack> stack = EntryStacks.of(item);
			registration.addWorkstations(BLOCK_EXPLODING, stack);
		}
	}

	@Override
	public void registerDisplays(DisplayRegistry registration) {
		registerFiller(registration, RecipeTypes.ITEM_BURNING, ItemBurningDisplay::new);
		registerFiller(registration, RecipeTypes.ITEM_INSIDE, ItemInsideDisplay::new);
		registerFiller(registration, RecipeTypes.BLOCK_INTERACTING, BlockInteractionDisplay::new);
		registerFiller(registration, RecipeTypes.BLOCK_CLICKING, BlockInteractionDisplay::new);
		registerFiller(registration, RecipeTypes.BLOCK_CRUSHING, BlockCrushingDisplay::new);
		registerFiller(registration, RecipeTypes.LIGHTNING_CHANNELING, ItemShapelessDisplay::new);
		registerFiller(registration, RecipeTypes.ITEM_EXPLODING, ItemShapelessDisplay::new);
		registerFiller(registration, RecipeTypes.BLOCK_EXPLODING, BlockExplodingDisplay::new);

		LUtil.recipes(RecipeTypes.ANVIL_CRAFTING).stream().filter($ -> {
			return !$.getResultItem().isEmpty() && !$.isSpecial();
		}).map($ -> {
			List<ItemStack> right = List.of($.getRight().getItems()).stream().map(ItemStack::copy).peek($$ -> $$.setCount($.getMaterialCost())).toList();
			return new AnvilRecipe($.getId(), List.of($.getLeft().getItems()), right, List.of($.getResultItem()));
		}).forEach(registration::add);
	}

	private static <T extends LycheeRecipe<?>, D extends Display> void registerFiller(DisplayRegistry registration, LycheeRecipeType<?, ? extends T> recipeType, Function<? extends T, D> filler) {
		registration.registerRecipeFiller((Class<T>) recipeType.clazz, type -> Objects.equals(recipeType, type), LycheeRecipe::showInRecipeViewer, filler);
	}

	@Override
	public void registerEntryTypes(EntryTypeRegistry registration) {
		registration.register(POST_ACTION, new PostActionIngredientHelper());
	}

	private static final Map<AllGuiTextures, ScreenElementWrapper> elMap = Maps.newIdentityHashMap();

	public static ScreenElementWrapper el(AllGuiTextures element) {
		return elMap.computeIfAbsent(element, ScreenElementWrapper::new);
	}

	public enum SlotType {
		NORMAL(AllGuiTextures.JEI_SLOT),
		CHANCE(AllGuiTextures.JEI_CHANCE_SLOT),
		CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

		final ScreenElement element;

		private SlotType(AllGuiTextures element) {
			this.element = el(element).element;
		}
	}

	public static LEntryWidget slot(Point startPoint, int x, int y, SlotType slotType) {
		LEntryWidget widget = new LEntryWidget(new Point(startPoint.x + x + 1, startPoint.y + y + 1));
		widget.background(slotType.element);
		return widget;
	}

	public static class ScreenElementWrapper extends WidgetWithBounds {

		public final Rectangle bounds = new Rectangle(16, 16);
		private final ScreenElement element;

		private ScreenElementWrapper(AllGuiTextures element) {
			this.element = element;
			bounds.width = element.width;
			bounds.height = element.height;
		}

		public ScreenElementWrapper(RenderElement element) {
			this.element = element;
			bounds.width = element.getWidth();
			bounds.height = element.getHeight();
		}

		@Override
		public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
			element.render(poseStack, bounds.x, bounds.y);
		}

		@Override
		public Rectangle getBounds() {
			return bounds;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

	}

	public static Rectangle offsetRect(Point startPoint, Rect2i rect) {
		return new Rectangle(startPoint.x + rect.getX(), startPoint.y + rect.getY(), rect.getWidth(), rect.getHeight());
	}

}
