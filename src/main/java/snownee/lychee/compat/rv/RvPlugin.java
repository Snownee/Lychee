package snownee.lychee.compat.rv;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.rei.category.ItemAndBlockBaseCategory;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class RvPlugin {
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	private final Map<ResourceLocation, RvCategoryType<?>> categoryTypes = Maps.newHashMap();
	private final Map<ResourceLocation, RvCategory<?>> categories = Maps.newHashMap();
	private final String name = STACK_WALKER.getCallerClass().getSimpleName();

	public void init() {
		categoryTypes.clear();
		categories.clear();
		register(
				RecipeTypes.BLOCK_INTERACTING, it -> {
					it.iconProvider = category -> {
						var mainIcon = category.recipes.stream().map($ -> $.value().getType()).anyMatch($ -> $ ==
								RecipeTypes.BLOCK_INTERACTING) ? AllGuiTextures.RIGHT_CLICK : AllGuiTextures.LEFT_CLICK;
						return Either.left(new SideBlockIcon(
								mainIcon,
								Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock(category.recipes))));
					};
				});
		//TODO Other register calls

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
	}

	public void registerCategories(Consumer<RvCategory<?>> consumer) {
		categories.values().forEach(consumer);
	}

	public void registerWorkstations(RvCategoryProvider<?> category, Consumer<List<ItemStack>> consumer) {
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
