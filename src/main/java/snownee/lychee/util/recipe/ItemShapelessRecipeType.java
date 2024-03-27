package snownee.lychee.util.recipe;

import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.Lychee;
import snownee.lychee.context.ItemShapelessContext;
import snownee.lychee.context.RecipeContext;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public class ItemShapelessRecipeType<R extends ILycheeRecipe<LycheeContext>> extends LycheeRecipeType<LycheeContext, R> {
	private final ValidItemCache validItems = new ValidItemCache();

	public ItemShapelessRecipeType(
			String name,
			Class<R> clazz,
			@Nullable LootContextParamSet contextParamSet
	) {
		super(name, clazz, contextParamSet);
		compactInputs = true;
	}

	@Override
	public void refreshCache() {
		super.refreshCache();
		validItems.refreshCache(recipes);
	}

	public void process(
			final Stream<ItemEntity> itemEntities,
			final LycheeContext context
	) {
		if (isEmpty()) {
			return;
		}
		final var list = itemEntities.filter($ -> validItems.contains($.getItem())).collect(Collectors.toCollection(LinkedList::new));
		context.put(LycheeContextKey.ITEM_SHAPELESS, new ItemShapelessContext(list, context));
		context.get(LycheeContextKey.LOOT_PARAMS).validate(contextParamSet);
		process(this, recipes, context, null);
	}

	public static <T extends ILycheeRecipe<LycheeContext>> void process(
			final LycheeRecipeType<LycheeContext, T> recipeType,
			final Iterable<RecipeHolder<T>> recipes,
			final LycheeContext context,
			final Predicate<RecipeHolder<T>> predicate
	) {
		var matchedAny = false;
		var loop = 0;
		final var excluded = Sets.newHashSet();
		var level = context.get(LycheeContextKey.LEVEL);
		var itemShapelessContext = context.get(LycheeContextKey.ITEM_SHAPELESS);
		final var actionContext = context.get(LycheeContextKey.ACTION);
		major:
		while (true) {
			var matched = false;
			for (final var recipe : recipes) {
				// recipe without ingredients will only run once to prevent dead loop
				if (recipe.value().getIngredients().isEmpty() && loop > 0) {
					continue;
				}
				if (excluded.contains(recipe)) {
					continue;
				}
				try {
					final var match = recipeType.tryMatch(recipe, level, context);
					if (match.isPresent()) {
						if (predicate != null && !predicate.test(recipe)) {
							excluded.add(recipe);
							continue;
						}
						context.put(LycheeContextKey.RECIPE_ID, new RecipeContext(recipe.id()));
						context.put(LycheeContextKey.RECIPE, recipe.value());
						matchedAny = matched = true;
						var times = 1;
						final var matcher = itemShapelessContext.getMatcher();
						if (matcher.map(it -> it.inputUsed.length > 0).orElse(false)) {
							var inputUsed = matcher.get().inputUsed;
							times = recipe.value().getRandomRepeats(Integer.MAX_VALUE, context);
							for (var i = 0; i < inputUsed.length; i++) {
								if (inputUsed[i] > 0) {
									var stack = itemShapelessContext.filteredItems.get(i).getItem();
									times = Math.min(times, stack.getCount() / inputUsed[i]);
								}
							}
						}
						match.get().value().applyPostActions(context, times);
						if (matcher.isPresent()) {
							itemShapelessContext.totalItems -= context.get(LycheeContextKey.ITEM).postApply(
									!actionContext.avoidDefault,
									times);
						}
						if (!recipe.value().maxRepeats().isAny()) {
							break major;
						}
						itemShapelessContext.filteredItems = null;
						itemShapelessContext.setMatcher(null);
						itemShapelessContext.itemEntities.removeIf(it -> it.getItem().isEmpty());
					}
				} catch (Exception e) {
					Lychee.LOGGER.error("", e);
					break major;
				}
			}
			if (++loop >= 100 || !matched) {
				break;
			}
		}
		if (matchedAny) {
			itemShapelessContext.itemEntities.forEach(it -> it.setItem(it.getItem())); //sync item amount
		}
	}
}
