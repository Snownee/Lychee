package snownee.lychee.core.recipe.type;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.Lychee;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class ItemShapelessRecipeType<C extends ItemShapelessContext, T extends LycheeRecipe<C>> extends LycheeRecipeType<C, T> {

	private ValidItemCache validItems = new ValidItemCache();

	public ItemShapelessRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet contextParamSet) {
		super(name, clazz, contextParamSet);
		compactInputs = true;
	}

	@Override
	public void buildCache() {
		super.buildCache();
		validItems.buildCache(recipes);
	}

	public void process(Level level, Stream<ItemEntity> itemEntities, Consumer<ItemShapelessContext.Builder<C>> ctxBuilderConsumer) {
		if (isEmpty()) {
			return;
		}
		List<ItemEntity> list = itemEntities.filter($ -> validItems.contains($.getItem())).collect(Collectors.toCollection(LinkedList::new));
		ItemShapelessContext.Builder<C> ctxBuilder = new ItemShapelessContext.Builder<>(level, list);
		ctxBuilderConsumer.accept(ctxBuilder);
		process(this, recipes, ctxBuilder.create(contextParamSet), null);
	}

	public static <C extends ItemShapelessContext, T extends LycheeRecipe<C>> void process(LycheeRecipeType<C, T> recipeType, Iterable<T> recipes, C ctx, Predicate<T> test) {
		boolean matchedAny = false;
		int loop = 0;
		Set<T> excluded = Sets.newHashSet();
		major:
		while (true) {
			boolean matched = false;
			for (T recipe : recipes) {
				// recipe without ingredients will only run once to prevent dead loop
				if (recipe.getIngredients().isEmpty() && loop > 0) {
					continue;
				}
				if (excluded.contains(recipe)) {
					continue;
				}
				try {
					Optional<T> match = recipeType.tryMatch(recipe, ctx.getLevel(), ctx);
					if (match.isPresent()) {
						if (test != null && !test.test(recipe)) {
							excluded.add(recipe);
							continue;
						}
						matchedAny = matched = true;
						int times = 1;
						if (ctx.match != null && ctx.match.length > 0) {
							times = recipe.getRandomRepeats(Integer.MAX_VALUE, ctx);
							for (int i = 0; i < ctx.match.length; i++) {
								if (ctx.match[i] > 0) {
									ItemStack stack = ctx.filteredItems.get(i).getItem();
									times = Math.min(times, stack.getCount() / ctx.match[i]);
								}
							}
						}
						if (match.get().applyPostActions(ctx, times) && ctx.match != null) {
							for (int i = 0; i < ctx.match.length; i++) {
								if (ctx.match[i] > 0) {
									ItemEntity itemEntity = ctx.filteredItems.get(i);
									int count = ctx.match[i] * times;
									itemEntity.getItem().shrink(count);
									ctx.totalItems -= count;
								}
							}
						}
						if (!recipe.getMaxRepeats().isAny()) {
							break major;
						}
						ctx.filteredItems = null;
						ctx.match = null;
						ctx.itemEntities.removeIf($ -> $.getItem().isEmpty());
					}
				} catch (Exception e) {
					Lychee.LOGGER.catching(e);
					break major;
				}
			}
			if (++loop >= 100 || !matched) {
				break major;
			}
		}
		if (matchedAny) {
			ctx.itemEntities.forEach($ -> $.setItem($.getItem())); //sync item amount
		}
	}

}
