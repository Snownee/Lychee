package snownee.lychee.compat.jei;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.jei.category.BlockCrushingRecipeCategory;
import snownee.lychee.compat.jei.category.BlockExplodingRecipeCategory;
import snownee.lychee.compat.jei.category.BlockInteractionRecipeCategory;
import snownee.lychee.compat.jei.category.ItemBurningRecipeCategory;
import snownee.lychee.compat.jei.category.ItemInsideRecipeCategory;
import snownee.lychee.compat.jei.category.ItemShapelessRecipeCategory;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientHelper;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.core.post.PostAction;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(Lychee.ID, "main");
	public static final IIngredientType<PostAction> POST_ACTION = () -> PostAction.class;
	public static IJeiRuntime RUNTIME;

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new ItemBurningRecipeCategory(RecipeTypes.ITEM_BURNING, guiHelper));
		registration.addRecipeCategories(new ItemInsideRecipeCategory<>(RecipeTypes.ITEM_INSIDE, guiHelper, AllGuiTextures.JEI_DOWN_ARROW));
		ScreenElement mainIcon = RecipeTypes.BLOCK_INTERACTING.isEmpty() ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		registration.addRecipeCategories(new BlockInteractionRecipeCategory((List) List.of(RecipeTypes.BLOCK_INTERACTING, RecipeTypes.BLOCK_CLICKING), guiHelper, mainIcon));
		registration.addRecipeCategories(new BlockCrushingRecipeCategory(RecipeTypes.BLOCK_CRUSHING, guiHelper));
		registration.addRecipeCategories(new ItemShapelessRecipeCategory<>(RecipeTypes.LIGHTNING_CHANNELING, guiHelper, guiHelper.createDrawableIngredient(VanillaTypes.ITEM, Items.LIGHTNING_ROD.getDefaultInstance())));
		registration.addRecipeCategories(new ItemShapelessRecipeCategory<>(RecipeTypes.ITEM_EXPLODING, guiHelper, guiHelper.createDrawableIngredient(VanillaTypes.ITEM, Items.TNT.getDefaultInstance())));
		registration.addRecipeCategories(new BlockExplodingRecipeCategory(guiHelper, GuiGameElement.of(Items.TNT)));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(RecipeTypes.ITEM_BURNING.recipes(), RecipeTypes.ITEM_BURNING.id);
		registration.addRecipes(RecipeTypes.ITEM_INSIDE.recipes(), RecipeTypes.ITEM_INSIDE.id);
		registration.addRecipes(RecipeTypes.BLOCK_INTERACTING.recipes(), RecipeTypes.BLOCK_INTERACTING.id);
		registration.addRecipes(RecipeTypes.BLOCK_CLICKING.recipes(), RecipeTypes.BLOCK_INTERACTING.id);
		registration.addRecipes(RecipeTypes.BLOCK_CRUSHING.recipes(), RecipeTypes.BLOCK_CRUSHING.id);
		registration.addRecipes(RecipeTypes.LIGHTNING_CHANNELING.recipes(), RecipeTypes.LIGHTNING_CHANNELING.id);
		registration.addRecipes(RecipeTypes.ITEM_EXPLODING.recipes(), RecipeTypes.ITEM_EXPLODING.id);
		registration.addRecipes(RecipeTypes.BLOCK_EXPLODING.recipes(), RecipeTypes.BLOCK_EXPLODING.id);

		List<IJeiAnvilRecipe> recipes = RecipeTypes.ANVIL_CRAFTING.recipes().stream().filter($ -> {
			return !$.getResultItem().isEmpty() && !$.isSpecial();
		}).map($ -> {
			List<ItemStack> right = List.of($.getRight().getItems()).stream().map(ItemStack::copy).peek($$ -> $$.setCount($.getMaterialCost())).toList();
			return registration.getVanillaRecipeFactory().createAnvilRecipe(List.of($.getLeft().getItems()), right, List.of($.getResultItem()));
		}).toList();
		registration.addRecipes(recipes, VanillaRecipeCategoryUid.ANVIL);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		registration.register(POST_ACTION, List.of(), new PostActionIngredientHelper(), PostActionIngredientRenderer.INSTANCE);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		for (ItemStack stack : RecipeTypes.BLOCK_CRUSHING.blockKeysToItems()) {
			registration.addRecipeCatalyst(VanillaTypes.ITEM, stack, RecipeTypes.BLOCK_CRUSHING.id);
		}
		registration.addRecipeCatalyst(VanillaTypes.ITEM, Items.LIGHTNING_ROD.getDefaultInstance(), RecipeTypes.LIGHTNING_CHANNELING.id);
		for (Item item : LycheeTags.EXPLOSIVES.getValues()) {
			ItemStack stack = item.getDefaultInstance();
			registration.addRecipeCatalyst(VanillaTypes.ITEM, stack, RecipeTypes.ITEM_EXPLODING.id);
			registration.addRecipeCatalyst(VanillaTypes.ITEM, stack, RecipeTypes.BLOCK_EXPLODING.id);
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		RUNTIME = jeiRuntime;
	}

	private static final Map<AllGuiTextures, IDrawable> elMap = Maps.newIdentityHashMap();

	public static IDrawable slot(boolean chance) {
		return chance ? el(AllGuiTextures.JEI_CHANCE_SLOT) : el(AllGuiTextures.JEI_SLOT);
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
