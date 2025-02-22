package snownee.lychee.compat.jei;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.KUtil;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.jei.category.AbstractLycheeCategory;
import snownee.lychee.compat.jei.category.CategoryProviders;
import snownee.lychee.compat.jei.category.CraftingRecipeCategoryExtension;
import snownee.lychee.compat.jei.category.IconProviders;
import snownee.lychee.compat.jei.category.WorkstationRegisters;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientHelper;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
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
	private final Multimap<AbstractLycheeCategory<?>, RecipeHolder<? extends ILycheeRecipe<LycheeContext>>> categories = LinkedHashMultimap.create();
	public static IJeiRuntime runtime;
	public static IJeiHelpers helpers;

	public static IDrawable slot(SlotType slotType) {
		return slotType.element;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		categories.clear();
		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			var generatedCategories = JEIREI.generateCategories(recipeType, RecipeType::createRecipeHolderType);

			var categoryProvider = CategoryProviders.get(recipeType);

			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", recipeType);
				continue;
			}

			var guiHelper = registry.getJeiHelpers().getGuiHelper();

			generatedCategories.forEach((id, recipes) -> {
				var registeredCategory = categories.keys().stream().filter(it -> it.getRecipeType().getUid().equals(id.getUid())).findAny();

				var category = registeredCategory.orElseGet(() -> categoryProvider.get(
						(RecipeType) id,
						Objects.requireNonNull(IconProviders.get(recipeType), recipeType::toString).get(guiHelper, recipes),
						(List) recipes,
						guiHelper));
				categories.putAll(category, recipes);
				if (registeredCategory.isEmpty()) {
					registry.addRecipeCategories(category);
				}
			});
		}
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		registration.getCraftingCategory().addExtension(ShapedCraftingRecipe.class, new CraftingRecipeCategoryExtension());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		helpers = registry.getJeiHelpers();
		categories.asMap().forEach((category, recipes) -> registry.addRecipes(
				(RecipeType) category.getRecipeType(),
				recipes.stream().toList()));

		try {
			var recipes = KUtil.getRecipes(RecipeTypes.ANVIL_CRAFTING).stream().filter($ ->
					!$.value().output().isEmpty() && !$.value().isSpecial() && !$.value().hideInRecipeViewer()).map($ -> {
				var recipe = $.value();
				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				List<ItemStack> right = ingredients.size() == 1 ? List.of() : Stream.of(ingredients.getLast().getItems())
						.map(ItemStack::copy)
						.peek(it -> it.setCount(recipe.materialCost()))
						.toList();
				return registry.getVanillaRecipeFactory().createAnvilRecipe(
						List.of(recipe.getIngredients().getFirst().getItems()),
						right,
						List.of(recipe.output()),
						$.id());
			}).toList();
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
		categories.asMap().forEach((category, recipes) -> {
			var workstationRegister = WorkstationRegisters.get(category.recipeType());
			if (workstationRegister != null) {
				workstationRegister.consume(registry, (AbstractLycheeCategory) category, (List) recipes.stream().toList());
			}
		});
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
			this.element = elementMap.computeIfAbsent(element, ScreenElementWrapper::new);
		}
	}
}
