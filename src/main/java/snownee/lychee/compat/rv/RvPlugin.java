package snownee.lychee.compat.rv;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.rv.category.IItemShapelessRecipeCategory;
import snownee.lychee.compat.rv.category.ItemAndBlockCategory;
import snownee.lychee.compat.rv.category.RvCategory;
import snownee.lychee.compat.rv.category.RvCategoryType;
import snownee.lychee.compat.rv.element.InfoElementHelper;
import snownee.lychee.compat.rv.element.SideBlockIcon;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.RectExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class RvPlugin {
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private final Map<ResourceLocation, RvCategoryType<?>> categoryTypes = Maps.newHashMap();
	private ImmutableMap<ResourceLocation, RvCategory<?>> categories = ImmutableMap.of();
	private final String name = STACK_WALKER.getCallerClass().getSimpleName();

	public void init() {
		categoryTypes.clear();
		var categories = Maps.<ResourceLocation, RvCategory<?>>newHashMap();
		register(
				RecipeTypes.BLOCK_CRUSHING, type -> {
					type.width = RvCategoryType.WIDTH + 20;
					type.iconProvider = category -> GuiGameElement.of(Items.ANVIL);
					type.setSimpleWorkstationProvider(category -> category.recipes().stream()
							.map(it -> it.value().blockPredicate())
							.filter(it -> !BlockPredicateExtensions.isAny(it))
							.distinct()
							.flatMap($ -> BlockPredicateExtensions.matchedBlocks($).stream())
							.distinct()
							.map(ItemStack::new)
							.toList());
				});
		register(
				RecipeTypes.BLOCK_EXPLODING, it -> {
					it.infoRect = IItemShapelessRecipeCategory.INFO_RECT;
					it.iconProvider = category -> {
						var mainIcon = GuiGameElement.of(Items.TNT.getDefaultInstance());
						return new SideBlockIcon(mainIcon, Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
					};
					it.setSimpleWorkstationProvider(category ->
							CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.BLOCK_EXPLODING_CATALYSTS)
									.stream()
									.map(ItemStack::new)
									.toList());
				});
		register(
				RecipeTypes.BLOCK_INTERACTING, type -> {
					type.infoRect = RectExtensions.offsetRect(ItemAndBlockCategory.INFO_RECT, 10, 0);
					type.width = RvCategoryType.WIDER_WIDTH;
					type.iconProvider = category -> {
						var mainIcon = category.recipes().stream()
								.map(it -> it.value().getType())
								.anyMatch(it -> it == RecipeTypes.BLOCK_INTERACTING)
								? AllGuiTextures.RIGHT_CLICK
								: AllGuiTextures.LEFT_CLICK;
						return new SideBlockIcon(mainIcon, Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
					};
				});
		register(
				RecipeTypes.DRIPSTONE_DRIPPING,
				it -> {
					it.infoRect = RectExtensions.offsetRect(it.infoRect, -10, 0);
					it.iconProvider = category -> GuiGameElement.of(Items.POINTED_DRIPSTONE);
					it.setSimpleWorkstationProvider(category -> List.of(Items.POINTED_DRIPSTONE.getDefaultInstance()));
				});
		register(
				RecipeTypes.LIGHTNING_CHANNELING,
				it -> {
					it.width = RvCategoryType.WIDER_WIDTH;
					it.infoRect = IItemShapelessRecipeCategory.INFO_RECT;
					it.iconProvider = category -> GuiGameElement.of(Items.LIGHTNING_ROD);
					it.setSimpleWorkstationProvider(category -> List.of(Items.LIGHTNING_ROD.getDefaultInstance()));
				});
		register(
				RecipeTypes.ITEM_EXPLODING, it -> {
					it.width = RvCategoryType.WIDER_WIDTH;
					it.infoRect = IItemShapelessRecipeCategory.INFO_RECT;
					it.iconProvider = category -> GuiGameElement.of(Items.TNT);
					it.setSimpleWorkstationProvider(category -> CommonProxy.tagElements(
									BuiltInRegistries.ITEM,
									LycheeTags.ITEM_EXPLODING_CATALYSTS)
							.stream()
							.map(ItemStack::new)
							.toList());
				});
		register(
				RecipeTypes.ITEM_BURNING, it -> {
					it.iconProvider = category ->
							new SideBlockIcon(AllGuiTextures.DOWN_ARROW, Suppliers.memoize(Blocks.FIRE::defaultBlockState));
				});
		register(
				RecipeTypes.ITEM_INSIDE, it -> {
					it.width = RvCategoryType.WIDER_WIDTH;
					it.infoRect = InfoElementHelper.getInfoRect(4, 25);
					it.iconProvider = category ->
							new SideBlockIcon(AllGuiTextures.DOWN_ARROW, Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
				});

		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			Function<ResourceLocation, RvCategory<?>> factory = $ -> new RvCategory<>(categoryTypes.get(recipeType.categoryId), $);
			for (var recipe : recipeType.inViewerRecipes()) {
				var id = RVs.composeCategoryIdentifier(recipeType.categoryId, ResourceLocation.parse(recipe.value().group()));
				categories.computeIfAbsent(id, factory).addRecipe(recipe);
			}
		}

		this.categories = ImmutableMap.copyOf(categories);
	}

	public ImmutableMap<ResourceLocation, RvCategory<?>> categories() {
		return categories;
	}

	public void registerCategories(Consumer<RvCategory<?>> consumer) {
		categories.values().forEach(consumer);
	}

	public void registerWorkstations(RvCategoryProvider<?> category, Consumer<List<List<ItemStack>>> consumer) {
		consumer.accept(category.rvCategory().workstations());
	}

	private <T extends ILycheeRecipe<LycheeContext>> void register(LycheeRecipeType<T> recipeType, Consumer<RvCategoryType<T>> configurer) {
		var type = new RvCategoryType<T>(recipeType);
		configurer.accept(type);
		Preconditions.checkArgument(
				categoryTypes.put(recipeType.categoryId, type) == null,
				"Duplicate category type: %s",
				recipeType.categoryId);
	}

	@Override
	public String toString() {
		return "RvPlugin{" + name + "}";
	}
}
