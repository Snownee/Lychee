package snownee.lychee.compat.jei;

import java.util.List;
import java.util.Map;
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
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.Registry;
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
import snownee.lychee.compat.jei.category.BaseJEICategory;
import snownee.lychee.compat.jei.category.BlockCrushingRecipeCategory;
import snownee.lychee.compat.jei.category.BlockExplodingRecipeCategory;
import snownee.lychee.compat.jei.category.BlockInteractionRecipeCategory;
import snownee.lychee.compat.jei.category.ItemBurningRecipeCategory;
import snownee.lychee.compat.jei.category.ItemExplodingRecipeCategory;
import snownee.lychee.compat.jei.category.ItemInsideRecipeCategory;
import snownee.lychee.compat.jei.category.LightningChannelingRecipeCategory;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientHelper;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.util.LUtil;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(Lychee.ID, "main");
	public static final IIngredientType<PostAction> POST_ACTION = () -> PostAction.class;
	public static IJeiRuntime RUNTIME;
	public static IJeiHelpers HELPERS;
	public static IGuiHelper GUI;
	private ItemBurningRecipeCategory ITEM_BURNING;
	private ItemInsideRecipeCategory<ItemInsideRecipe> ITEM_INSIDE;
	private BlockInteractionRecipeCategory BLOCK_INTERACTING;
	private BlockCrushingRecipeCategory BLOCK_CRUSHING;
	private LightningChannelingRecipeCategory LIGHTNING_CHANNELING;
	private ItemExplodingRecipeCategory ITEM_EXPLODING;
	private BlockExplodingRecipeCategory BLOCK_EXPLODING;

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	public Stream<BaseJEICategory<LycheeContext, LycheeRecipe<LycheeContext>>> getCategories() {
		return (Stream<BaseJEICategory<LycheeContext, LycheeRecipe<LycheeContext>>>) (Object) Stream.of(ITEM_BURNING, ITEM_INSIDE, BLOCK_INTERACTING, BLOCK_CRUSHING, LIGHTNING_CHANNELING, ITEM_EXPLODING, BLOCK_EXPLODING);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		HELPERS = registration.getJeiHelpers();
		GUI = HELPERS.getGuiHelper();
		ITEM_BURNING = new ItemBurningRecipeCategory(RecipeTypes.ITEM_BURNING);
		ITEM_INSIDE = new ItemInsideRecipeCategory<>(RecipeTypes.ITEM_INSIDE, AllGuiTextures.JEI_DOWN_ARROW);
		ScreenElement mainIcon = RecipeTypes.BLOCK_INTERACTING.isEmpty() ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		BLOCK_INTERACTING = new BlockInteractionRecipeCategory((List) List.of(RecipeTypes.BLOCK_INTERACTING, RecipeTypes.BLOCK_CLICKING), mainIcon);
		BLOCK_CRUSHING = new BlockCrushingRecipeCategory(RecipeTypes.BLOCK_CRUSHING);
		LIGHTNING_CHANNELING = new LightningChannelingRecipeCategory(RecipeTypes.LIGHTNING_CHANNELING);
		ITEM_EXPLODING = new ItemExplodingRecipeCategory(RecipeTypes.ITEM_EXPLODING);
		BLOCK_EXPLODING = new BlockExplodingRecipeCategory(RecipeTypes.BLOCK_EXPLODING, GuiGameElement.of(Items.TNT));
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
			return !$.getResultItem().isEmpty() && !$.isSpecial();
		}).map($ -> {
			List<ItemStack> right = List.of($.getRight().getItems()).stream().map(ItemStack::copy).peek($$ -> $$.setCount($.getMaterialCost())).toList();
			return registration.getVanillaRecipeFactory().createAnvilRecipe(List.of($.getLeft().getItems()), right, List.of($.getResultItem()));
		}).toList();
		registration.addRecipes(mezz.jei.api.constants.RecipeTypes.ANVIL, recipes);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		registration.register(POST_ACTION, List.of(), new PostActionIngredientHelper(), PostActionIngredientRenderer.INSTANCE);
	}

	@SuppressWarnings("deprecation")
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
