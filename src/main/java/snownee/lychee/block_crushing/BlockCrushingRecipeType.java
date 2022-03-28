package snownee.lychee.block_crushing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.LycheeTags;
import snownee.lychee.core.network.SCustomLevelEventPacket;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.util.LUtil;

public class BlockCrushingRecipeType extends BlockKeyRecipeType<BlockCrushingContext, BlockCrushingRecipe> {

	private ValidItemCache validItems = new ValidItemCache();

	public BlockCrushingRecipeType(String name, Class<BlockCrushingRecipe> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	public void process(FallingBlockEntity entity) {
		if (isEmpty()) {
			return;
		}
		BlockPos pos = LUtil.getOnPos(entity);
		BlockState fallingBlock = entity.getBlockState();
		Collection<BlockCrushingRecipe> recipes = recipesByBlock.getOrDefault(fallingBlock.getBlock(), Collections.EMPTY_LIST);
		if (recipes.isEmpty()) {
			return;
		}
		AABB box = entity.getBoundingBox();
		Level level = entity.level;
		BlockState landingBlock = level.getBlockState(pos);
		if (landingBlock.is(LycheeTags.EXTEND_BOX)) {
			box = box.minmax(new AABB(pos));
		}
		List<ItemEntity> itemEntities = entity.level.getEntitiesOfClass(ItemEntity.class, box, $ -> {
			return $.isAlive() && validItems.contains(((ItemEntity) $).getItem());
		});
		BlockCrushingContext.Builder ctxBuilder = new BlockCrushingContext.Builder(entity.level, itemEntities, entity);
		ctxBuilder.withParameter(LootContextParams.ORIGIN, entity.position());
		ctxBuilder.withParameter(LootContextParams.THIS_ENTITY, entity);
		ctxBuilder.withParameter(LootContextParams.BLOCK_STATE, landingBlock);
		ctxBuilder.withParameter(LycheeLootContextParams.BLOCK_POS, pos);
		BlockCrushingContext ctx = ctxBuilder.create(contextParamSet);
		boolean matchedAny = false;
		int loop = 0;
		major:
		while (true) {
			boolean matched = false;
			for (BlockCrushingRecipe recipe : recipes) {
				// recipe without ingredients will only run once to prevent dead loop
				if (recipe.ingredients.isEmpty() && loop > 0) {
					continue;
				}
				try {
					Optional<BlockCrushingRecipe> match = tryMatch(recipe, level, ctx);
					if (match.isPresent()) {
						matchedAny = matched = true;
						int times = 1;
						if (ctx.match != null && ctx.match.length > 0) {
							//System.out.println(Arrays.toString(ctx.match));
							if (recipe.isRepeatable()) {
								times = Integer.MAX_VALUE;
								for (int i = 0; i < ctx.match.length; i++) {
									if (ctx.match[i] > 0) {
										ItemStack stack = ctx.filteredItems.get(i).getItem();
										times = Math.min(times, stack.getCount() / ctx.match[i]);
									}
								}
							}
							for (int i = 0; i < ctx.match.length; i++) {
								if (ctx.match[i] > 0) {
									ItemEntity itemEntity = ctx.filteredItems.get(i);
									if (Lychee.hasKiwi) {
										SCustomLevelEventPacket.sendItemParticles(itemEntity.getItem(), level, itemEntity.position());
									}
									int count = ctx.match[i] * times;
									itemEntity.getItem().shrink(count);
									ctx.totalItems -= count;
								}
							}
						}
						if (!match.get().applyPostActions(ctx, times)) {
							((LycheeFallingBlockEntity) entity).lychee$cancelDrop();
						}
						if (!recipe.isRepeatable()) {
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
			BlockState state = level.getBlockState(entity.blockPosition());
			if (!FallingBlock.isFree(state)) {
				entity.setPosRaw(entity.getX(), pos.getY() + 1, entity.getZ());
			}
			((LycheeFallingBlockEntity) entity).lychee$matched();
			ctx.itemEntities.forEach($ -> $.setItem($.getItem().copy())); //sync item amount
		}
	}

	@Override
	public void buildCache() {
		super.buildCache();
		validItems.buildCache(recipes);
	}

}