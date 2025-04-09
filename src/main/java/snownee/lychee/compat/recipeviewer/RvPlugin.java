package snownee.lychee.compat.recipeviewer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.category.BlockCrushingRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.BlockExplodingRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.BlockInteractingRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.DripstoneRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.ItemBurningRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.ItemExplodingRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.ItemInsideRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.LightningChannelingRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstanceProviders;
import snownee.lychee.compat.recipeviewer.element.SideBlockIcon;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class RvPlugin<Helper extends RvHelper> {
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private final Map<ResourceLocation, RvCategory<?>> categoryTypes = Maps.newHashMap();
	private ImmutableMap<ResourceLocation, RvCategoryInstance<?>> categories = ImmutableMap.of();
	private final String name = STACK_WALKER.getCallerClass().getSimpleName();

	private final Helper rvHelper;

	public RvPlugin(Helper rvHelper) {
		this.rvHelper = rvHelper;
	}

	public Helper helper() {
		return rvHelper;
	}

	public void init() {
		categoryTypes.clear();
		rvHelper.init();
		var categories = Maps.<ResourceLocation, RvCategoryInstance<?>>newHashMap();
		register(
				RecipeTypes.BLANK,
				new RvCategory<>(),
				it -> it.iconProvider = category -> RenderElement.empty());
		register(
				RecipeTypes.BLOCK_CRUSHING,
				new BlockCrushingRecipeCategory(),
				it -> {
					it.width = RvCategory.WIDER_WIDTH;
					it.iconProvider = category -> BlockCrushingRecipeCategory.icon(GuiGameElement.of(Items.ANVIL));
					it.setSimpleWorkstationProvider(category -> category.recipes().stream()
							.map($ -> $.value().blockPredicate())
							.filter($ -> !BlockPredicateExtensions.isAny($))
							.distinct()
							.flatMap($ -> BlockPredicateExtensions.matchedBlocks($).stream())
							.distinct()
							.map(ItemStack::new)
							.toList());
				});
		register(
				RecipeTypes.BLOCK_EXPLODING,
				new BlockExplodingRecipeCategory(),
				it -> {
					it.iconProvider = category -> {
						var mainIcon = GuiGameElement.of(Items.TNT);
						return new SideBlockIcon(mainIcon, Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
					};
					it.setSimpleWorkstationProvider(category -> List.of(Items.TNT.getDefaultInstance()));
				});
		register(
				RecipeTypes.BLOCK_INTERACTING,
				new BlockInteractingRecipeCategory(),
				type -> {
					type.width += 30;
					type.iconProvider = category -> {
						var mainIcon = category.recipes().stream()
								.map($ -> $.value().getType())
								.anyMatch($ -> $ == RecipeTypes.BLOCK_INTERACTING)
								? AllGuiTextures.RIGHT_CLICK
								: AllGuiTextures.LEFT_CLICK;
						return new SideBlockIcon(mainIcon, Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
					};
				});
		register(
				RecipeTypes.DRIPSTONE_DRIPPING,
				new DripstoneRecipeCategory(),
				it -> {
					it.iconProvider = category -> GuiGameElement.of(Items.POINTED_DRIPSTONE);
					it.setSimpleWorkstationProvider(category -> List.of(Items.POINTED_DRIPSTONE.getDefaultInstance()));
				});
		register(
				RecipeTypes.LIGHTNING_CHANNELING,
				new LightningChannelingRecipeCategory(),
				it -> {
					it.width = RvCategory.WIDER_WIDTH;
					it.iconProvider = category -> GuiGameElement.of(Items.LIGHTNING_ROD);
					it.setSimpleWorkstationProvider(category -> List.of(Items.LIGHTNING_ROD.getDefaultInstance()));
				});
		register(
				RecipeTypes.ITEM_EXPLODING,
				new ItemExplodingRecipeCategory(),
				it -> {
					it.width = RvCategory.WIDER_WIDTH;
					it.iconProvider = category -> GuiGameElement.of(Items.TNT);
					it.setSimpleWorkstationProvider(category -> List.of(Items.TNT.getDefaultInstance()));
				});
		register(
				RecipeTypes.ITEM_BURNING,
				new ItemBurningRecipeCategory(),
				it -> {
					it.iconProvider = category ->
							new SideBlockIcon(AllGuiTextures.DOWN_ARROW, Suppliers.memoize(Blocks.FIRE::defaultBlockState));
				});
		register(
				RecipeTypes.ITEM_INSIDE,
				new ItemInsideRecipeCategory(),
				it -> {
					it.width = RvCategory.WIDER_WIDTH;
					it.iconProvider = category -> new SideBlockIcon(
							AllGuiTextures.DOWN_ARROW,
							Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
				});

		for (var recipeType : RecipeTypes.ALL) {
			//noinspection unchecked
			RvCategory<ILycheeRecipe<LycheeContext>> category = (RvCategory<ILycheeRecipe<LycheeContext>>) categoryTypes.get(recipeType.categoryId);
			if (category == null) {
				continue;
			}

			var provider = RvCategoryInstanceProviders.get(recipeType.categoryId);
			if (provider == null) {
				continue;
			}

			var factory = provider.get(category, rvHelper);
			for (var recipe : recipeType.inViewerRecipes()) {
				var id = RVs.composeCategoryIdentifier(recipeType.categoryId, ResourceLocation.parse(recipe.value().group()));
				//noinspection unchecked,rawtypes
				categories.computeIfAbsent(id, factory).addRecipe((RecipeHolder) recipe);
			}
		}

		this.categories = ImmutableMap.copyOf(categories);
	}

	public ImmutableMap<ResourceLocation, RvCategoryInstance<?>> categories() {
		return categories;
	}

	public <R extends ILycheeRecipe<LycheeContext>, T extends RvCategory<R>> void register(
			LycheeRecipeType<R> recipeType,
			T category,
			Consumer<T> configurer) {
		category.id = recipeType.categoryId;
		configurer.accept(category);
		Preconditions.checkNotNull(category.iconProvider, "Icon provider is null: %s", category.id);
		category.init();
		Preconditions.checkArgument(
				categoryTypes.put(category.id, category) == null,
				"Duplicate category type: %s",
				category.id);
	}

	@Override
	public String toString() {
		return "RvPlugin{" + name + "}";
	}
}
