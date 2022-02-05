package snownee.lychee.block_crushing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;

public class BlockCrushingRecipeType extends BlockKeyRecipeType<ItemShapelessContext, BlockCrushingRecipe> {

	private IntSet validItems;

	public BlockCrushingRecipeType(String name, Class<BlockCrushingRecipe> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	public void process(FallingBlockEntity entity, BlockPos pos, Vec3 origin) {
		if (isEmpty()) {
			return;
		}
		BlockState fallingBlock = entity.getBlockState();
		Collection<BlockCrushingRecipe> recipes = recipesByBlock.getOrDefault(fallingBlock.getBlock(), Collections.EMPTY_LIST);
		if (recipes.isEmpty()) {
			return;
		}
		Level level = entity.level;
		List<ItemEntity> itemEntities = entity.level.getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox(), $ -> {
			return $.isAlive() && validItems.contains(StackedContents.getStackingIndex(((ItemEntity) $).getItem()));
		});
		BlockState landingBlock = level.getBlockState(pos);
		ItemShapelessContext.Builder ctxBuilder = new ItemShapelessContext.Builder(entity.level, itemEntities, entity);
		ctxBuilder.withParameter(LootContextParams.ORIGIN, origin);
		ctxBuilder.withParameter(LootContextParams.THIS_ENTITY, entity);
		ctxBuilder.withParameter(LootContextParams.BLOCK_STATE, landingBlock);
		ctxBuilder.withParameter(LycheeLootContextParams.BLOCK_POS, pos);
		if (landingBlock.hasBlockEntity()) {
			ctxBuilder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));
		}
		ItemShapelessContext ctx = ctxBuilder.create(contextParamSet);
		boolean matched = false;
		do {
			for (BlockCrushingRecipe recipe : recipes) {
				Optional<BlockCrushingRecipe> match = tryMatch(recipe, level, ctx);
				if (match.isPresent()) {
					matched = true;
					match.get().applyPostActions(ctx, 1); //TODO
					ctx.itemEntities.removeIf($ -> $.getItem().isEmpty());
				}
			}
		} while (matched);
	}

	@Override
	public void buildCache() {
		super.buildCache();
		validItems = new IntAVLTreeSet(recipes.stream().flatMap($ -> {
			return $.getIngredients().stream();
		}).flatMapToInt($ -> {
			return $.getStackingIds().intStream();
		}).toArray());
	}

}