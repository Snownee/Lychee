package snownee.lychee.core.recipe.type;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
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
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.LycheeCounter;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.Pair;

public class BlockKeyRecipeType<C extends LycheeContext, T extends ItemAndBlockRecipe<C>> extends LycheeRecipeType<C, T> {

	protected final Multimap<Block, T> multimap = HashMultimap.create();
	protected final List<T> anyBlockRecipes = Lists.newLinkedList();

	public BlockKeyRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	public void buildCache(RecipeManager recipeManager) {
		multimap.clear();
		anyBlockRecipes.clear();
		for (T recipe : recipes(recipeManager)) {
			if (recipe.getBlock() == BlockPredicate.ANY) {
				anyBlockRecipes.add(recipe);
				continue;
			}
			for (Block block : BlockPredicateHelper.getMatchedBlocks(recipe.getBlock())) {
				multimap.put(block, recipe);
			}
		}
	}

	public Pair<BlockState, Integer> getMostUsedBlock() {
		Entry<Block, Collection<T>> most = null;
		for (Entry<Block, Collection<T>> entry : multimap.asMap().entrySet()) {
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

	public Optional<T> process(Entity entity, ItemStack stack, BlockPos pos, Vec3 origin, LycheeContext.Builder<C> ctxBuilder) {
		if (isEmpty()) {
			return Optional.empty();
		}
		Level level = entity.level;
		BlockState blockstate = level.getBlockState(pos);
		Collection<T> recipes = multimap.get(blockstate.getBlock());
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
		T prevRecipe = null;
		{
			ResourceLocation prevRecipeId = ((LycheeCounter) entity).lychee$getRecipeId();
			if (prevRecipeId != null) {
				Recipe<?> recipe = LUtil.recipe(prevRecipeId);
				if (recipe != null && recipe.getType() == this) {
					prevRecipe = (T) recipe;
					if (tryMatchAndApply(prevRecipe, level, ctx, stack)) {
						return Optional.of(prevRecipe);
					}
				}
			}
		}
		for (T recipe : Iterables.concat(recipes, anyBlockRecipes)) {
			if (recipe == prevRecipe) {
				continue;
			}
			if (tryMatchAndApply(recipe, level, ctx, stack)) {
				return Optional.of(recipe);
			}
		}
		return Optional.empty();
	}

	private boolean tryMatchAndApply(T recipe, Level level, C ctx, ItemStack stack) {
		if (tryMatch(recipe, level, ctx).isPresent()) {
			if (!level.isClientSide && recipe.tickOrApply(ctx)) {
				int times = recipe.isRepeatable() ? stack.getCount() : 1;
				if (recipe.applyPostActions(ctx, times)) {
					stack.shrink(times);
				}
			}
			return true;
		}
		return false;
	}

}
