package snownee.lychee.compat.recipeviewer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.util.KUtil;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.category.RvCategoryProviders;
import snownee.lychee.compat.recipeviewer.category.RvCategoryType;
import snownee.lychee.compat.recipeviewer.element.SideBlockIcon;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;
import snownee.lychee.util.ui.CategoryMetadata;

public class RvPlugin<Helper extends RvHelper> {
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private final Map<ResourceLocation, RvCategoryType<?>> categoryTypes = Maps.newHashMap();
	private ImmutableMap<ResourceLocation, RvCategory<?>> categories = ImmutableMap.of();
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
		var categories = Maps.<ResourceLocation, RvCategory<?>>newHashMap();
		register(
				RecipeTypes.BLOCK_CRUSHING, type -> {
					type.width = RvCategoryType.WIDER_WIDTH;
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
					type.width += 30;
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
					it.iconProvider = category -> GuiGameElement.of(Items.POINTED_DRIPSTONE);
					it.setSimpleWorkstationProvider(category -> List.of(Items.POINTED_DRIPSTONE.getDefaultInstance()));
				});
		register(
				RecipeTypes.LIGHTNING_CHANNELING,
				it -> {
					it.width = RvCategoryType.WIDER_WIDTH;
					it.iconProvider = category -> GuiGameElement.of(Items.LIGHTNING_ROD);
					it.setSimpleWorkstationProvider(category -> List.of(Items.LIGHTNING_ROD.getDefaultInstance()));
				});
		register(
				RecipeTypes.ITEM_EXPLODING, it -> {
					it.width = RvCategoryType.WIDER_WIDTH;
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
					it.iconProvider = category -> new SideBlockIcon(
							AllGuiTextures.DOWN_ARROW,
							Suppliers.memoize(() -> RVs.getIconBlock(category.recipes())));
				});

		List<RecipeHolder<CategoryMetadata>> metadataList = KUtil.getRecipes(RecipeTypes.CATEGORY_METADATA);

		for (var recipeType : RecipeTypes.ALL) {
			var provider = RvCategoryProviders.get(recipeType.categoryId);
			if (provider == null) {
				continue;
			}

			//noinspection unchecked
			var factory = provider.get((RvCategoryType<ILycheeRecipe<LycheeContext>>) categoryTypes.get(recipeType.categoryId), rvHelper)
					.andThen(it -> {
						String id = it.id().toString();
						for (RecipeHolder<CategoryMetadata> metadata : metadataList) {
							for (Pattern pattern : metadata.value().categoryPattern()) {
								if (pattern.matcher(id).matches()) {
									it.setMetadata(metadata.value());
									return it;
								}
							}
						}
						return it;
					});
			for (var recipe : recipeType.inViewerRecipes()) {
				var id = RVs.composeCategoryIdentifier(recipeType.categoryId, ResourceLocation.parse(recipe.value().group()));
				//noinspection unchecked,rawtypes
				categories.computeIfAbsent(id, factory).addRecipe((RecipeHolder) recipe);
			}
		}

		this.categories = ImmutableMap.copyOf(categories);
	}

	public ImmutableMap<ResourceLocation, RvCategory<?>> categories() {
		return categories;
	}

	public <T extends ILycheeRecipe<LycheeContext>> void register(LycheeRecipeType<T> recipeType, Consumer<RvCategoryType<T>> configurer) {
		var type = new RvCategoryType<T>(recipeType.categoryId);
		configurer.accept(type);
		Preconditions.checkNotNull(type.iconProvider, "Icon provider is null: %s", recipeType.categoryId);
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
