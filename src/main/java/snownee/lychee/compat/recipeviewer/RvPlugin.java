package snownee.lychee.compat.recipeviewer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
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

public class RvPlugin<Helper extends RvHelper> {
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private final Map<Identifier, RvCategory<?>> categoryTypes = Maps.newHashMap();
	private ImmutableMap<Identifier, RvCategoryInstance<?>> categories = ImmutableMap.of();
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
		var categories = Maps.<Identifier, RvCategoryInstance<?>>newHashMap();
		register(
				new RvCategory<>(RecipeTypes.BLANK),
				it -> it.iconProvider = _ -> RenderElement.empty());
		register(
				new BlockCrushingRecipeCategory(),
				it -> {
					it.width = RvCategory.WIDER_WIDTH;
					it.iconProvider = _ -> BlockCrushingRecipeCategory.icon(GuiGameElement.of(Items.ANVIL));
					it.setSimpleWorkstationProvider(category -> category.recipes().stream()
							.map($ -> $.value().blockPredicate())
							.filter($ -> !BlockPredicateExtensions.isAny($))
							.distinct()
							.flatMap($ -> BlockPredicateExtensions.matchedBlocks($).stream())
							.distinct()
							.map(Block::asItem)
							.toList());
				});
		register(
				new BlockExplodingRecipeCategory(),
				it -> {
					it.iconProvider = category -> {
						var mainIcon = GuiGameElement.of(Items.TNT);
						return new SideBlockIcon(mainIcon, Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
					};
					it.setSimpleWorkstationProvider(_ -> List.of(Items.TNT));
				});
		register(
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
				new DripstoneRecipeCategory(),
				it -> {
					it.iconProvider = _ -> GuiGameElement.of(Items.POINTED_DRIPSTONE);
					it.setSimpleWorkstationProvider(_ -> List.of(Items.POINTED_DRIPSTONE));
				});
		register(
				new LightningChannelingRecipeCategory(),
				it -> {
					it.width = RvCategory.WIDER_WIDTH;
					it.iconProvider = _ -> GuiGameElement.of(Items.LIGHTNING_ROD);
					it.setSimpleWorkstationProvider(_ -> List.of(Items.LIGHTNING_ROD));
				});
		register(
				new ItemExplodingRecipeCategory(),
				it -> {
					it.width = RvCategory.WIDER_WIDTH;
					it.iconProvider = _ -> GuiGameElement.of(Items.TNT);
					it.setSimpleWorkstationProvider(_ -> List.of(Items.TNT));
				});
		register(
				new ItemBurningRecipeCategory(),
				it -> {
					it.iconProvider = _ ->
							new SideBlockIcon(AllGuiTextures.DOWN_ARROW, Suppliers.memoize(Blocks.FIRE::defaultBlockState));
				});
		register(
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
				var id = RVs.composeCategoryIdentifier(recipeType.categoryId, Identifier.parse(recipe.value().group()));
				//noinspection unchecked,rawtypes
				categories.computeIfAbsent(id, factory).addRecipe((RecipeHolder) recipe);
			}
		}

		this.categories = ImmutableMap.copyOf(categories);
	}

	public ImmutableMap<Identifier, RvCategoryInstance<?>> categories() {
		return categories;
	}

	public <R extends ILycheeRecipe<LycheeContext>, T extends RvCategory<R>> void register(T category, Consumer<T> configurer) {
		configurer.accept(category);
		Preconditions.checkNotNull(category.iconProvider, "Icon provider is null: %s", category.id());
		category.init();
		Preconditions.checkArgument(
				categoryTypes.put(category.id(), category) == null,
				"Duplicate category type: %s",
				category.id());
	}

	@Override
	public String toString() {
		return "RvPlugin{" + name + "}";
	}
}
