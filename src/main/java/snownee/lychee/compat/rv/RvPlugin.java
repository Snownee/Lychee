package snownee.lychee.compat.rv;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.util.CommonProxy;
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
		Map<ResourceLocation, RvCategory<?>> categories = Maps.newHashMap();
		register(
				RecipeTypes.BLOCK_CRUSHING, it -> {
					it.iconProvider = category -> Either.right(Items.ANVIL.getDefaultInstance());
					it.setSimpleWorkstationProvider(category -> category.recipes.stream()
							.<BlockPredicate>mapMulti((recipe, consumer) -> {
								var blockPredicate = recipe.value().blockPredicate();
								if (!BlockPredicateExtensions.isAny(blockPredicate)) {
									consumer.accept(blockPredicate);
								}
							})
							.distinct()
							.flatMap($ -> BlockPredicateExtensions.matchedBlocks($).stream())
							.distinct()
							.map(ItemStack::new)
							.toList());
				});
		register(
				RecipeTypes.BLOCK_EXPLODING, it -> {
					it.iconProvider = category -> {
						var mainIcon = GuiGameElement.of(Items.TNT.getDefaultInstance());
						return Either.left(new SideBlockIcon(
								mainIcon,
								Suppliers.memoize(() -> RVs.getIconBlock(category.recipes))));
					};
					it.setSimpleWorkstationProvider(category -> CommonProxy.tagElements(
									BuiltInRegistries.ITEM,
									LycheeTags.BLOCK_EXPLODING_CATALYSTS)
							.stream()
							.map(ItemStack::new)
							.toList());
				});
		register(
				RecipeTypes.BLOCK_INTERACTING, it -> {
					it.iconProvider = category -> {
						var mainIcon = category.recipes.stream()
								.map($ -> $.value().getType())
								.anyMatch($ -> $ == RecipeTypes.BLOCK_INTERACTING)
								? AllGuiTextures.RIGHT_CLICK
								: AllGuiTextures.LEFT_CLICK;
						return Either.left(new SideBlockIcon(
								mainIcon,
								Suppliers.memoize(() -> RVs.getIconBlock(category.recipes))));
					};
				});
		register(
				RecipeTypes.DRIPSTONE_DRIPPING,
				it -> {
					it.iconProvider = category -> Either.right(Items.POINTED_DRIPSTONE.getDefaultInstance());
					it.setSimpleWorkstationProvider(category -> List.of(Items.POINTED_DRIPSTONE.getDefaultInstance()));
				});
		register(
				RecipeTypes.LIGHTNING_CHANNELING,
				it -> {
					it.iconProvider = category -> Either.right(Items.LIGHTNING_ROD.getDefaultInstance());
					it.setSimpleWorkstationProvider(category -> List.of(Items.LIGHTNING_ROD.getDefaultInstance()));
				});
		register(
				RecipeTypes.ITEM_EXPLODING, it -> {
					it.iconProvider = category -> Either.right(Items.TNT.getDefaultInstance());
					it.setSimpleWorkstationProvider(category -> CommonProxy.tagElements(
									BuiltInRegistries.ITEM,
									LycheeTags.ITEM_EXPLODING_CATALYSTS)
							.stream()
							.map(ItemStack::new)
							.toList());
				});
		register(
				RecipeTypes.ITEM_BURNING, it -> {
					it.iconProvider = category -> Either.left(new SideBlockIcon(
							AllGuiTextures.JEI_DOWN_ARROW,
							Suppliers.memoize(Blocks.FIRE::defaultBlockState)));
				});
		register(
				RecipeTypes.ITEM_INSIDE, it -> {
					it.iconProvider = category -> Either.left(new SideBlockIcon(
							AllGuiTextures.JEI_DOWN_ARROW,
							Suppliers.memoize(() -> RVs.getIconBlock(category.recipes))));
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

	private <T extends ILycheeRecipe<LycheeContext>> void register(LycheeRecipeType<T> recipeType, Consumer<RvCategoryType<T>> consumer) {
		var type = new RvCategoryType<T>(recipeType.categoryId);
		consumer.accept(type);
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
