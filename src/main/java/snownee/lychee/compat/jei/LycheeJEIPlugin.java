package snownee.lychee.compat.jei;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.util.KUtil;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.jei.category.CategoryProviders;
import snownee.lychee.compat.jei.category.CraftingRecipeCategoryExtension;
import snownee.lychee.compat.jei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.jei.elements.ScreenElementWidget;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientHelper;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.compat.rv.RvPlugin;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

@REIPluginCompatIgnore
@JeiPlugin
@NotNullByDefault
public class LycheeJEIPlugin implements IModPlugin {
	public static final ResourceLocation ID = Lychee.id("main");
	public static final IIngredientType<PostAction> POST_ACTION = () -> PostAction.class;
	private static final Map<AllGuiTextures, IDrawable> elementMap = Maps.newIdentityHashMap();
	public static IJeiRuntime runtime;
	public static IJeiHelpers helpers;
	private final RvPlugin rvPlugin = new RvPlugin();

	public static IDrawable slot(SlotType slotType) {
		return slotType.element;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		var guiHelper = registry.getJeiHelpers().getGuiHelper();
		rvPlugin.init();
		for (RvCategory<?> rvCategory : rvPlugin.categories().values()) {
			var categoryProvider = CategoryProviders.get(rvCategory.type.id);
			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", rvCategory.type.id);
				continue;
			}

			//noinspection unchecked
			var category = categoryProvider.get(
					RecipeType.createRecipeHolderType(rvCategory.id),
					(RvCategory<ILycheeRecipe<LycheeContext>>) rvCategory,
					guiHelper);
			registry.addRecipeCategories(category);
		}
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		registration.getCraftingCategory().addExtension(ShapedCraftingRecipe.class, new CraftingRecipeCategoryExtension());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		helpers = registry.getJeiHelpers();
		for (RvCategory<?> rvCategory : rvPlugin.categories().values()) {
			//noinspection unchecked,rawtypes
			registry.addRecipes((RecipeType) registry.getJeiHelpers().getRecipeType(rvCategory.id).orElseThrow(), rvCategory.recipes);
		}

		try {
			var recipes = KUtil.getRecipes(RecipeTypes.ANVIL_CRAFTING)
					.stream()
					.filter($ ->
							!$.value().output().isEmpty() && !$.value().isSpecial() && !$.value().hideInRecipeViewer())
					.map($ -> (IJeiAnvilRecipe) AnvilCraftingDisplay.of($))
					.toList();
			registry.addRecipes(mezz.jei.api.constants.RecipeTypes.ANVIL, recipes);
		} catch (Throwable e) {
			Lychee.LOGGER.error("Error when registering anvil crafting recipes", e);
		}
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		registration.register(
				POST_ACTION,
				List.of(),
				new PostActionIngredientHelper(),
				PostActionIngredientRenderer.INSTANCE,
				PostAction.CODEC);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		for (RvCategory<?> rvCategory : rvPlugin.categories().values()) {
			RecipeType<?> recipeType = registry.getJeiHelpers().getRecipeType(rvCategory.id).orElseThrow();
			for (List<ItemStack> workstation : rvCategory.workstations()) {
				registry.addRecipeCatalysts(recipeType, VanillaTypes.ITEM_STACK, workstation);
			}
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
		Minecraft.getInstance().execute(() -> {
			var recipes = KUtil.getRecipes(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream().filter($ ->
					$.value() instanceof ILycheeRecipe<?> recipe && recipe.hideInRecipeViewer()).toList();
			jeiRuntime.getRecipeManager().hideRecipes(mezz.jei.api.constants.RecipeTypes.CRAFTING, recipes);
		});
	}

	public enum SlotType {
		NORMAL(AllGuiTextures.JEI_SLOT), CHANCE(AllGuiTextures.JEI_CHANCE_SLOT), CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

		final IDrawable element;

		SlotType(AllGuiTextures element) {
			this.element = elementMap.computeIfAbsent(element, ScreenElementWidget::new);
		}
	}
}
