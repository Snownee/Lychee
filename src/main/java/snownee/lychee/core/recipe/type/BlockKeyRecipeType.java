package snownee.lychee.core.recipe.type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.LycheeCounter;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.Pair;

public class BlockKeyRecipeType<C extends LycheeContext, T extends LycheeRecipe<C> & BlockKeyRecipe> extends LycheeRecipeType<C, T> {

	protected final Map<Block, List<T>> recipesByBlock = Maps.newHashMap();
	protected final List<T> anyBlockRecipes = Lists.newLinkedList();

	public BlockKeyRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	public void buildCache() {
		recipesByBlock.clear();
		anyBlockRecipes.clear();
		super.buildCache();
		Multimap<Block, T> multimap = HashMultimap.create();
		for (T recipe : recipes) {
			if (recipe.getBlock() == BlockPredicate.ANY) {
				anyBlockRecipes.add(recipe);
				continue;
			}
			for (Block block : BlockPredicateHelper.getMatchedBlocks(recipe.getBlock())) {
				multimap.put(block, recipe);
			}
		}
		for (Entry<Block, Collection<T>> e : multimap.asMap().entrySet()) {
			List<T> list = Lists.newArrayList(e.getValue());
			list.sort(null);
			recipesByBlock.put(e.getKey(), list);
		}
	}

	public Pair<BlockState, Integer> getMostUsedBlock() {
		Entry<Block, List<T>> most = null;
		for (Entry<Block, List<T>> entry : recipesByBlock.entrySet()) {
			if (most == null || most.getValue().size() < entry.getValue().size()) {
				most = entry;
			}
		}
		if (most == null) {
			return Pair.of(Blocks.AIR.defaultBlockState(), 0);
		}
		BlockState state = BlockPredicateHelper.anyBlockState(most.getValue().stream().findAny().get().getBlock());
		return Pair.of(state, most.getValue().size());
	}

	public List<ItemStack> blockKeysToItems() {
		return recipesByBlock.keySet().stream().map(Block::asItem).filter($ -> {
			return $ != Items.AIR;
		}).sorted((a, b) -> {
			return Integer.compare(Item.getId(a), Item.getId(b));
		}).map(ItemStack::new).toList();
	}

	public Optional<T> process(Entity entity, ItemStack stack, BlockPos pos, Vec3 origin, LycheeContext.Builder<C> ctxBuilder) {
		if (isEmpty()) {
			return Optional.empty();
		}
		ResourceLocation prevRecipeId = null;
		if (entity instanceof LycheeCounter) {
			prevRecipeId = ((LycheeCounter) entity).lychee$getRecipeId();
			((LycheeCounter) entity).lychee$setRecipeId(null);
		}
		Level level = entity.level;
		BlockState blockstate = level.getBlockState(pos);
		Collection<T> recipes = recipesByBlock.getOrDefault(blockstate.getBlock(), Collections.EMPTY_LIST);
		if (recipes.isEmpty() && anyBlockRecipes.isEmpty()) {
			return Optional.empty();
		}
		ctxBuilder.withParameter(LootContextParams.ORIGIN, origin);
		ctxBuilder.withParameter(LootContextParams.THIS_ENTITY, entity);
		ctxBuilder.withParameter(LootContextParams.BLOCK_STATE, blockstate);
		ctxBuilder.withParameter(LycheeLootContextParams.BLOCK_POS, pos);
		if (blockstate.hasBlockEntity()) {
			ctxBuilder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));
		}
		C ctx = ctxBuilder.create(contextParamSet);
		T prevRecipe = (T) Optional.ofNullable(prevRecipeId).map(LUtil::recipe).filter($ -> $.getType() == this).orElse(null);
		Iterable<T> iterable = Iterables.concat(recipes, anyBlockRecipes);
		if (prevRecipe != null) {
			iterable = Iterables.concat(List.of(prevRecipe), Iterables.filter(iterable, $ -> $ != prevRecipe));
		}
		for (T recipe : iterable) {
			if (tryMatch(recipe, level, ctx).isPresent()) {
				if (entity instanceof LycheeCounter) {
					((LycheeCounter) entity).lychee$update(prevRecipeId, recipe);
				}
				if (!level.isClientSide && recipe.tickOrApply(ctx)) {
					int times = recipe.isRepeatable() ? stack.getCount() : 1;
					if (recipe.applyPostActions(ctx, times)) {
						stack.shrink(times);
					}
				}
				return Optional.of(recipe);
			}
		}
		return Optional.empty();
	}

}
