package snownee.lychee.compat.jei;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.JEIREI.CategoryCreationContext;
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
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.crafting.ShapedCraftingRecipe;
import snownee.lychee.util.CommonProxy;

@JeiPlugin
public class JEICompat implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(Lychee.ID, "main");
	public static final IIngredientType<PostAction> POST_ACTION = () -> PostAction.class;
	public static IJeiRuntime RUNTIME;
	public static IJeiHelpers HELPERS;
	public static IGuiHelper GUI;
	public static final Map<ResourceLocation, Map<ResourceLocation, BaseJEICategory<?, ?>>> CATEGORIES = Maps.newHashMap();

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		HELPERS = registration.getJeiHelpers();
		GUI = HELPERS.getGuiHelper();

		Map<ResourceLocation, Function<CategoryCreationContext, BaseJEICategory<?, ?>>> factories = Maps.newHashMap();
		factories.put(RecipeTypes.ITEM_BURNING.categoryId, $ -> new ItemBurningRecipeCategory(RecipeTypes.ITEM_BURNING));
		factories.put(RecipeTypes.ITEM_INSIDE.categoryId, $ -> new ItemInsideRecipeCategory(RecipeTypes.ITEM_INSIDE, AllGuiTextures.JEI_DOWN_ARROW));
		factories.put(RecipeTypes.BLOCK_INTERACTING.categoryId, $ -> {
			ScreenElement mainIcon = $.recipes().stream().map(LycheeRecipe::getType).anyMatch(Predicate.isEqual(RecipeTypes.BLOCK_INTERACTING)) ? AllGuiTextures.RIGHT_CLICK : AllGuiTextures.LEFT_CLICK;
			return new BlockInteractionRecipeCategory((List) List.of(RecipeTypes.BLOCK_INTERACTING, RecipeTypes.BLOCK_CLICKING), mainIcon);
		});
		factories.put(RecipeTypes.BLOCK_CRUSHING.categoryId, $ -> new BlockCrushingRecipeCategory(RecipeTypes.BLOCK_CRUSHING));
		factories.put(RecipeTypes.LIGHTNING_CHANNELING.categoryId, $ -> new LightningChannelingRecipeCategory(RecipeTypes.LIGHTNING_CHANNELING));
		factories.put(RecipeTypes.ITEM_EXPLODING.categoryId, $ -> new ItemExplodingRecipeCategory(RecipeTypes.ITEM_EXPLODING));
		factories.put(RecipeTypes.BLOCK_EXPLODING.categoryId, $ -> new BlockExplodingRecipeCategory(RecipeTypes.BLOCK_EXPLODING, GuiGameElement.of(Items.TNT)));
		factories.put(RecipeTypes.DRIPSTONE_DRIPPING.categoryId, $ -> new DripstoneRecipeCategory(RecipeTypes.DRIPSTONE_DRIPPING));

		CATEGORIES.clear();
		JEIREI.registerCategories(factories::containsKey, (categoryId, context) -> {
			BaseJEICategory<?, ?> category = factories.get(categoryId).apply(context);
			category.recipeType = new mezz.jei.api.recipe.RecipeType(JEIREI.composeCategoryIdentifier(categoryId, context.group()), category.recipeTypes.get(0).clazz);
			category.initialRecipes = (List) context.recipes();
			category.icon = category.createIcon(GUI, (List) context.recipes());
			registration.addRecipeCategories(category);
			CATEGORIES.computeIfAbsent(categoryId, $ -> Maps.newHashMap()).put(context.group(), category);
		});
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		CATEGORIES.values().forEach($ -> {
			$.values().forEach($$ -> {
				@SuppressWarnings("rawtypes")
				BaseJEICategory category = (BaseJEICategory) $$;
				registration.addRecipes(category.recipeType, category.initialRecipes);
			});
		});

		try {
			List<IJeiAnvilRecipe> recipes = CommonProxy.recipes(RecipeTypes.ANVIL_CRAFTING).stream().filter($ -> {
				return !$.getResultItem().isEmpty() && !$.isSpecial() && $.showInRecipeViewer();
			}).map($ -> {
				List<ItemStack> right = Stream.of($.getRight().getItems()).map(ItemStack::copy).peek($$ -> $$.setCount($.getMaterialCost())).toList();
				return registration.getVanillaRecipeFactory().createAnvilRecipe(List.of($.getLeft().getItems()), right, List.of($.getResultItem()));
			}).toList();
			registration.addRecipes(mezz.jei.api.constants.RecipeTypes.ANVIL, recipes);
		} catch (Throwable e) {
			Lychee.LOGGER.error("", e);
		}
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
		forEachCategories(RecipeTypes.BLOCK_CRUSHING, $ -> {
			/* off */
			$.initialRecipes.stream()
					.map(BlockKeyRecipe::getBlock)
					.distinct()
					.map(BlockPredicateHelper::getMatchedBlocks)
					.flatMap(Collection::stream)
					.distinct()
					.map(ItemLike::asItem)
					.filter(Predicate.not(Items.AIR::equals))
					.map(Item::getDefaultInstance)
					.forEach($$ -> registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, $$, $.getRecipeType()));
			/* on */
		});
		forEachCategories(RecipeTypes.LIGHTNING_CHANNELING, $ -> {
			registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, Items.LIGHTNING_ROD.getDefaultInstance(), $.getRecipeType());
		});
		for (Item item : CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.ITEM_EXPLODING_CATALYSTS)) {
			forEachCategories(RecipeTypes.ITEM_EXPLODING, $ -> {
				registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), $.getRecipeType());
			});
		}
		for (Item item : CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.BLOCK_EXPLODING_CATALYSTS)) {
			forEachCategories(RecipeTypes.BLOCK_EXPLODING, $ -> {
				registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), $.getRecipeType());
			});
		}
		forEachCategories(RecipeTypes.DRIPSTONE_DRIPPING, $ -> {
			registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, Items.POINTED_DRIPSTONE.getDefaultInstance(), $.getRecipeType());
		});
	}

	private static <C extends LycheeContext, T extends LycheeRecipe<C>> void forEachCategories(LycheeRecipeType<C, T> recipeType, Consumer<BaseJEICategory<C, T>> consumer) {
		CATEGORIES.getOrDefault(recipeType.categoryId, Map.of()).values().stream().map($ -> (BaseJEICategory<C, T>) $).forEach(consumer);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		RUNTIME = jeiRuntime;
		Minecraft.getInstance().execute(() -> {
			/* off */
			var recipes = CommonProxy.recipes(RecipeType.CRAFTING).stream()
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
		public void draw(GuiGraphics graphics, int x, int y) {
			element.render(graphics, x, y);
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
