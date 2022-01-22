package snownee.lychee.interaction;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;

public class BlockInteractingRecipe extends ItemAndBlockRecipe<LycheeContext> {

	public BlockInteractingRecipe(ResourceLocation id) {
		super(id);
		willBatchRun = false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.BLOCK_INTERACTING;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeTypes.BLOCK_INTERACTING;
	}

	private static final Set<Block> POSSIBLE_BLOCKS = Sets.newIdentityHashSet();

	public static void on(ItemEntity entity) {
		BlockPos pos = entity.blockPosition();
		BlockState blockstate = entity.level.getBlockState(pos);
		if (!POSSIBLE_BLOCKS.contains(blockstate.getBlock())) {
			return;
		}
		LycheeContext.Builder builder = new LycheeContext.Builder(entity.level);
		builder.withParameter(LootContextParams.ORIGIN, entity.position());
		builder.withParameter(LootContextParams.THIS_ENTITY, entity);
		builder.withParameter(LootContextParams.BLOCK_STATE, blockstate);
		builder.withParameter(LycheeLootContextParams.BLOCK_POS, pos);
		if (blockstate.hasBlockEntity()) {
			builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, entity.level.getBlockEntity(pos));
		}
		LycheeContext ctx = builder.create(LycheeLootContextParamSets.BLOCK_INTERACTION);
		entity.level.getRecipeManager().getRecipeFor(RecipeTypes.BLOCK_INTERACTING, ctx, entity.level).ifPresent($ -> {
			int times = $.willBatchRun() ? entity.getItem().getCount() : 1;
			entity.getItem().shrink(times);
			$.applyPostActions(ctx, times);
		});
	}

	public static void buildCache(Collection<BlockInteractingRecipe> recipes) {
		POSSIBLE_BLOCKS.clear();
		recipes.stream().flatMap($ -> BlockPredicateHelper.getMatchedBlocks($.block).stream()).forEach(POSSIBLE_BLOCKS::add);
	}

}
