package snownee.lychee.item_inside;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableObject;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.LycheeCounter;
import snownee.lychee.core.recipe.type.ItemShapelessRecipeType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public class ItemInsideRecipeType extends LycheeRecipeType<ItemShapelessContext, ItemInsideRecipe> {

	private List<ItemInsideRecipe> specialRecipes = Lists.newArrayList();
	private Multimap<Item, ItemInsideRecipe> recipesByItem = ArrayListMultimap.create();

	public ItemInsideRecipeType(String name, Class<ItemInsideRecipe> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
		compactInputs = true;
	}

	@Override
	public void buildCache() {
		specialRecipes.clear();
		recipesByItem.clear();
		super.buildCache();
		Object2FloatMap<Item> itemCount = new Object2FloatOpenHashMap<>();
		List<Cache> caches = recipes.stream().map($ -> $.buildCache(itemCount, specialRecipes)).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
		List<Item> sorted = itemCount.object2FloatEntrySet().stream().sorted((a, b) -> Float.compare(b.getFloatValue(), a.getFloatValue())).map(Object2FloatMap.Entry::getKey).toList();
		for (Item item : sorted) {
			caches.removeIf(cache -> {
				if (cache.ingredients.stream().anyMatch($ -> $.contains(item))) {
					recipesByItem.put(item, cache.recipe);
					return cache.ingredients.stream().peek($ -> $.remove(item)).anyMatch(Set::isEmpty);
				}
				return false;
			});
		}
	}

	public void process(Entity entity, ItemStack stack, BlockPos pos, Vec3 origin) {
		if (isEmpty()) {
			return;
		}
		MutableObject<ResourceLocation> prevRecipeId = new MutableObject<>();
		if (entity instanceof LycheeCounter) {
			prevRecipeId.setValue(((LycheeCounter) entity).lychee$getRecipeId());
			((LycheeCounter) entity).lychee$setRecipeId(null);
		}
		Collection<ItemInsideRecipe> recipes = recipesByItem.get(stack.getItem());
		if (recipes.isEmpty() && specialRecipes.isEmpty()) {
			return;
		}
		Level level = entity.level;
		BlockState blockstate = level.getBlockState(pos);
		Block block = blockstate.getBlock();
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, AABB.ofSize(origin, 3, 3, 3), $ -> {
			if ($.isRemoved()) {
				return false;
			}
			if (!pos.equals($.blockPosition()) && !level.getBlockState($.blockPosition()).is(block)) {
				return false;
			}
			return true;
		});
		ItemShapelessContext.Builder<ItemShapelessContext> ctxBuilder = new ItemShapelessContext.Builder<>(level, items);
		ctxBuilder.withParameter(LootContextParams.ORIGIN, LUtil.clampPos(origin, pos));
		ctxBuilder.withParameter(LootContextParams.THIS_ENTITY, entity);
		ctxBuilder.withParameter(LootContextParams.BLOCK_STATE, blockstate);
		ctxBuilder.withParameter(LycheeLootContextParams.BLOCK_POS, pos);
		ItemShapelessContext ctx = ctxBuilder.create(contextParamSet);
		ItemInsideRecipe prevRecipe = (ItemInsideRecipe) Optional.ofNullable(prevRecipeId.getValue()).map(LUtil::recipe).filter($ -> $.getType() == this).orElse(null);
		Iterable<ItemInsideRecipe> iterable = Iterables.concat(recipes, specialRecipes);
		if (prevRecipe != null) {
			iterable = Iterables.concat(List.of(prevRecipe), Iterables.filter(iterable, $ -> $ != prevRecipe));
		}
		ItemShapelessRecipeType.process(this, iterable, ctx, recipe -> {
			((LycheeCounter) entity).lychee$update(prevRecipeId.getValue(), recipe);
			return recipe.tickOrApply(ctx);
		});
	}

	static record Cache(ItemInsideRecipe recipe, List<Set<Item>> ingredients) {
	}

}
