package snownee.lychee.core.recipe.type;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

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
		if (list.isEmpty()) {
			return;
		}
		ItemShapelessContext.Builder<C> ctxBuilder = new ItemShapelessContext.Builder<>(level, list);
		ctxBuilderConsumer.accept(ctxBuilder);
		C ctx = ctxBuilder.create(contextParamSet);
		boolean matchedAny = false;
		major:
		while (true) {
			boolean matched = false;
			for (T recipe : recipes) {
				try {
					Optional<T> match = tryMatch(recipe, level, ctx);
					if (match.isPresent()) {
						matchedAny = matched = true;
						int times = 1;
						if (ctx.match != null && ctx.match.length > 0) {
							if (recipe.isRepeatable()) {
								times = Integer.MAX_VALUE;
								for (int i = 0; i < ctx.match.length; i++) {
									if (ctx.match[i] > 0) {
										ItemStack stack = ctx.itemEntities.get(i).getItem();
										times = Math.min(times, stack.getCount() / ctx.match[i]);
									}
								}
							}
						}
						if (match.get().applyPostActions(ctx, times) && ctx.match != null) {
							for (int i = 0; i < ctx.match.length; i++) {
								if (ctx.match[i] > 0) {
									ItemEntity itemEntity = ctx.itemEntities.get(i);
									int count = ctx.match[i] * times;
									itemEntity.getItem().shrink(count);
									ctx.totalItems -= count;
								}
							}
						}
						if (!recipe.isRepeatable()) {
							break major;
						}
						ctx.match = null;
						ctx.itemEntities.removeIf($ -> $.getItem().isEmpty());
					}
				} catch (Exception e) {
					Lychee.LOGGER.catching(e);
					break major;
				}
			}
			if (!matched) {
				break major;
			}
		}
		if (matchedAny) {
			ctx.itemEntities.forEach($ -> $.setItem($.getItem())); //sync item amount
		}
	}

}
