package snownee.lychee.compat.jei;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.jei.category.BaseJEICategory;
import snownee.lychee.compat.jei.category.BlockCrushingRecipeCategory;
import snownee.lychee.compat.jei.category.BlockExplodingRecipeCategory;
import snownee.lychee.compat.jei.category.BlockInteractionRecipeCategory;
import snownee.lychee.compat.jei.category.CraftingRecipeCategoryExtension;
import snownee.lychee.compat.jei.category.DripstoneRecipeCategory;
import snownee.lychee.compat.jei.category.ItemBurningRecipeCategory;
import snownee.lychee.compat.jei.category.ItemExplodingRecipeCategory;
import snownee.lychee.compat.jei.category.ItemInsideRecipeCategory;
import snownee.lychee.compat.jei.category.LightningChannelingRecipeCategory;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientHelper;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.crafting.ShapedCraftingRecipe;
import snownee.lychee.util.LUtil;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(Lychee.ID, "main");
	public static final IIngredientType<PostAction> POST_ACTION = () -> PostAction.class;
	public static IJeiRuntime RUNTIME;
	public static IJeiHelpers HELPERS;
	public static IGuiHelper GUI;
	public static ItemBurningRecipeCategory ITEM_BURNING;
	public static ItemInsideRecipeCategory ITEM_INSIDE;
	public static BlockInteractionRecipeCategory BLOCK_INTERACTING;
	public static BlockCrushingRecipeCategory BLOCK_CRUSHING;
	public static LightningChannelingRecipeCategory LIGHTNING_CHANNELING;
	public static ItemExplodingRecipeCategory ITEM_EXPLODING;
	public static BlockExplodingRecipeCategory BLOCK_EXPLODING;
	public static DripstoneRecipeCategory DRIPSTONE_DRIPPING;

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	public Stream<BaseJEICategory<LycheeContext, LycheeRecipe<LycheeContext>>> getCategories() {
		return (Stream<BaseJEICategory<LycheeContext, LycheeRecipe<LycheeContext>>>) (Object) Stream.of(ITEM_BURNING, ITEM_INSIDE, BLOCK_INTERACTING, BLOCK_CRUSHING, LIGHTNING_CHANNELING, ITEM_EXPLODING, BLOCK_EXPLODING, DRIPSTONE_DRIPPING);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		HELPERS = registration.getJeiHelpers();
		GUI = HELPERS.getGuiHelper();
		ITEM_BURNING = new ItemBurningRecipeCategory(RecipeTypes.ITEM_BURNING);
		ITEM_INSIDE = new ItemInsideRecipeCategory(RecipeTypes.ITEM_INSIDE, AllGuiTextures.JEI_DOWN_ARROW);
		ScreenElement mainIcon = RecipeTypes.BLOCK_INTERACTING.isEmpty() ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		BLOCK_INTERACTING = new BlockInteractionRecipeCategory((List) List.of(RecipeTypes.BLOCK_INTERACTING, RecipeTypes.BLOCK_CLICKING), mainIcon);
		BLOCK_CRUSHING = new BlockCrushingRecipeCategory(RecipeTypes.BLOCK_CRUSHING);
		LIGHTNING_CHANNELING = new LightningChannelingRecipeCategory(RecipeTypes.LIGHTNING_CHANNELING);
		ITEM_EXPLODING = new ItemExplodingRecipeCategory(RecipeTypes.ITEM_EXPLODING);
		BLOCK_EXPLODING = new BlockExplodingRecipeCategory(RecipeTypes.BLOCK_EXPLODING, GuiGameElement.of(Items.TNT));
		DRIPSTONE_DRIPPING = new DripstoneRecipeCategory(RecipeTypes.DRIPSTONE_DRIPPING);
		getCategories().forEach(registration::addRecipeCategories);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		getCategories().forEach($ -> {
			$.recipeTypes.forEach($$ -> {
				registration.addRecipes($.getRecipeType(), LUtil.recipes($$).stream().filter(LycheeRecipe::showInRecipeViewer).toList());
			});
		});

		List<IJeiAnvilRecipe> recipes = LUtil.recipes(RecipeTypes.ANVIL_CRAFTING).stream().filter($ -> {
			return !$.getResultItem().isEmpty() && !$.isSpecial() && $.showInRecipeViewer();
		}).map($ -> {
			List<ItemStack> right = List.of($.getRight().getItems()).stream().map(ItemStack::copy).peek($$ -> $$.setCount($.getMaterialCost())).toList();
			return registration.getVanillaRecipeFactory().createAnvilRecipe(List.of($.getLeft().getItems()), right, List.of($.getResultItem()));
		}).toList();
		registration.addRecipes(mezz.jei.api.constants.RecipeTypes.ANVIL, recipes);
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		registration.getCraftingCategory().addCategoryExtension(ShapedCraftingRecipe.class, CraftingRecipeCategoryExtension::new);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		registration.register(POST_ACTION, List.of(), new PostActionIngredientHelper(), PostActionIngredientRenderer.INSTANCE);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		for (ItemStack stack : RecipeTypes.BLOCK_CRUSHING.blockKeysToItems()) {
			registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, stack, BLOCK_CRUSHING.getRecipeType());
		}
		registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, Items.LIGHTNING_ROD.getDefaultInstance(), LIGHTNING_CHANNELING.getRecipeType());
		for (Item item : LUtil.tagElements(Registry.ITEM, LycheeTags.ITEM_EXPLODING_CATALYSTS)) {
			registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), ITEM_EXPLODING.getRecipeType());
		}
		for (Item item : LUtil.tagElements(Registry.ITEM, LycheeTags.BLOCK_EXPLODING_CATALYSTS)) {
			registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), BLOCK_EXPLODING.getRecipeType());
		}
		registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, Items.POINTED_DRIPSTONE.getDefaultInstance(), DRIPSTONE_DRIPPING.getRecipeType());
	}

	@SuppressWarnings("static-access")
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		RUNTIME = jeiRuntime;
		Minecraft.getInstance().execute(() -> {
			/* off */
			var recipes = LUtil.recipes(RecipeType.CRAFTING).stream()
					.filter(ILycheeRecipe.class::isInstance)
					.map(ILycheeRecipe.class::cast)
					.filter(Predicate.not(ILycheeRecipe::showInRecipeViewer))
					.map(CraftingRecipe.class::cast)
					.toList();
			/* on */
			jeiRuntime.getRecipeManager().hideRecipes(mezz.jei.api.constants.RecipeTypes.CRAFTING, recipes);
		});
	}

	private static final Map<AllGuiTextures, IDrawable> elMap = Maps.newIdentityHashMap();

	public enum SlotType {
		NORMAL(AllGuiTextures.JEI_SLOT),
		CHANCE(AllGuiTextures.JEI_CHANCE_SLOT),
		CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

		final IDrawable element;

		SlotType(AllGuiTextures element) {
			this.element = el(element);
		}
	}

	public static IDrawable slot(SlotType slotType) {
		return slotType.element;
	}

	public static IDrawable el(AllGuiTextures element) {
		return elMap.computeIfAbsent(element, ScreenElementWrapper::new);
	}

	public static class ScreenElementWrapper implements IDrawable {

		private int width = 16;
		private int height = 16;
		private final ScreenElement element;

		private ScreenElementWrapper(AllGuiTextures element) {
			this.element = element;
			width = element.width;
			height = element.height;
		}

		public ScreenElementWrapper(RenderElement element) {
			this.element = element;
			width = element.getWidth();
			height = element.getHeight();
		}

		@Override
		public void draw(PoseStack poseStack, int x, int y) {
			element.render(poseStack, x, y);
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getWidth() {
			return width;
		}

	}

}
