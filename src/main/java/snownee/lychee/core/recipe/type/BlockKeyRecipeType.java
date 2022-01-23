package snownee.lychee.core.recipe.type;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
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

	public Optional<T> process(Entity entity, ItemStack stack, BlockPos pos, Vec3 origin, Consumer<LycheeContext.Builder> builderConsumer) {
		Level level = entity.level;
		BlockState blockstate = level.getBlockState(pos);
		Collection<T> recipes = multimap.get(blockstate.getBlock());
		if (recipes.isEmpty() && anyBlockRecipes.isEmpty()) {
			return Optional.empty();
		}
		LycheeContext.Builder builder = new LycheeContext.Builder(level);
		builder.withParameter(LootContextParams.ORIGIN, origin);
		builder.withParameter(LootContextParams.THIS_ENTITY, entity);
		builder.withParameter(LootContextParams.BLOCK_STATE, blockstate);
		builder.withParameter(LycheeLootContextParams.BLOCK_POS, pos);
		if (blockstate.hasBlockEntity()) {
			builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));
		}
		if (builderConsumer != null)
			builderConsumer.accept(builder);
		LycheeContext ctx = builder.create(contextParamSet);
		for (T recipe : Iterables.concat(recipes, anyBlockRecipes)) {
			if (recipe.matches(ctx, level)) {
				int times = recipe.willBatchRun() ? stack.getCount() : 1;
				if (recipe.applyPostActions(ctx, times)) {
					stack.shrink(times);
				}
				return Optional.of(recipe);
			}
		}
		return Optional.empty();
	}

}
