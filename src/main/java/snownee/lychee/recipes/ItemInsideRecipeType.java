package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.context.ItemShapelessContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.IngredientUtils;
import snownee.lychee.util.LycheeCounter;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ItemShapelessRecipeType;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ItemInsideRecipeType extends LycheeRecipeType<LycheeContext, ItemInsideRecipe> {

	private final List<RecipeHolder<ItemInsideRecipe>> specialRecipes = Lists.newArrayList();
	private final Multimap<Item, RecipeHolder<ItemInsideRecipe>> recipesByItem = ArrayListMultimap.create();

	public ItemInsideRecipeType(
			String name, Class<ItemInsideRecipe> clazz, @Nullable LootContextParamSet contextParamSet) {
		super(name, clazz, contextParamSet);
		compactInputs = true;
	}

	@Override
	public void refreshCache() {
		specialRecipes.clear();
		recipesByItem.clear();
		super.refreshCache();
		final var itemWeights = new Object2FloatOpenHashMap<Item>();
		final var caches = recipes.stream()
				.map(recipeHolder ->
						new Cache(
								recipeHolder,
								IngredientUtils
										.flattenIngredients(recipeHolder.value().getIngredients())
										.peek(it -> {
											final var weight = 1F / it.size();
											for (final var itemStack : it)
												itemWeights.merge(itemStack.getItem(), weight, Float::sum);
										})
										.map(stacks -> stacks.stream().map(ItemStack::getItem).collect(Collectors.toSet()))
										.toList()
						)
				)
				.collect(Collectors.toList());
		final var weightedItems = itemWeights.object2FloatEntrySet()
				.stream()
				.sorted((a, b) -> Float.compare(b.getFloatValue(), a.getFloatValue()))
				.map(Object2FloatMap.Entry::getKey)
				.toList();
		for (final var item : weightedItems) {
			// For putting all the recipes to the `recipesByItem`
			caches.removeIf(cache -> {
				if (cache.ingredients().stream().anyMatch(it -> it.contains(item))) {
					recipesByItem.put(item, cache.recipe());
					return cache.ingredients().stream().peek(it -> it.remove(item)).anyMatch(Set::isEmpty);
				}
				return false;
			});
		}
	}

	public void process(final Entity entity, final ItemStack stack, final BlockPos pos, final Vec3 origin) {
		if (isEmpty()) {
			return;
		}

		ResourceLocation prevRecipeId;
		if (entity instanceof LycheeCounter counter) {
			prevRecipeId = counter.lychee$getRecipeId();
			counter.lychee$setRecipeId(null);
		} else {
			prevRecipeId = null;
		}

		final var recipes = recipesByItem.get(stack.getItem());
		if (recipes.isEmpty() && specialRecipes.isEmpty()) {
			return;
		}

		final var level = entity.level();
		final var blockState = level.getBlockState(pos);
		final var block = blockState.getBlock();
		final var itemEntities = level.getEntitiesOfClass(ItemEntity.class, AABB.ofSize(origin, 3, 3, 3), it -> {
			if (it.isRemoved()) {
				return false;
			}
			return pos.equals(it.blockPosition()) || level.getBlockState(it.blockPosition()).is(block);
		});

		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		var itemShapelessContext = new ItemShapelessContext(itemEntities, context);
		context.put(LycheeContextKey.ITEM_SHAPELESS, itemShapelessContext);
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.ORIGIN, CommonProxy.clampPos(origin, pos));
		lootParamsContext.setParam(LootContextParams.THIS_ENTITY, entity);
		lootParamsContext.setParam(LootContextParams.BLOCK_STATE, blockState);
		lootParamsContext.setParam(LycheeLootContextParams.BLOCK_POS, pos);
		lootParamsContext.validate(contextParamSet);
		@SuppressWarnings("unchecked") @Nullable final var prevRecipe =
				(RecipeHolder<ItemInsideRecipe>) Optional.ofNullable(prevRecipeId)
						.map(CommonProxy::recipe)
						.filter(it -> it.value() instanceof ItemInsideRecipe)
						.orElse(null);
		var allRecipes = Iterables.concat(recipes, specialRecipes);
		if (prevRecipe != null) {
			allRecipes = Iterables.concat(List.of(prevRecipe), Iterables.filter(allRecipes, it -> !it.equals(prevRecipe)));
		}
		ItemShapelessRecipeType.process(this, allRecipes, context, it -> {
			((LycheeCounter) entity).lychee$update(prevRecipeId, it);
			return it.value().tickOrApply(context);
		});
	}

	public record Cache(RecipeHolder<ItemInsideRecipe> recipe, List<Set<Item>> ingredients) {}
}
