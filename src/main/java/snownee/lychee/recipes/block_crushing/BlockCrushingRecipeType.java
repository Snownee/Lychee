package snownee.lychee.recipes.block_crushing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

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
import snownee.lychee.util.input.ItemStackHolder;
import snownee.lychee.core.network.SCustomLevelEventPacket;
import snownee.lychee.core.recipe.recipe.type.BlockKeyRecipeType;
import snownee.lychee.util.CommonProxy;

public class BlockCrushingRecipeType extends BlockKeyRecipeType<BlockCrushingContext, BlockCrushingRecipe> {

	private ValidItemCache validItems = new ValidItemCache();

	public BlockCrushingRecipeType(
			String name,
			Class<BlockCrushingRecipe> clazz,
			@Nullable LootContextParamSet paramSet
	) {
		super(name, clazz, paramSet);
		compactInputs = true;
	}

	public void process(FallingBlockEntity entity) {
		if (isEmpty()) {
			return;
		}
		BlockPos pos = CommonProxy.getOnPos(entity);
		BlockState fallingBlock = entity.getBlockState();
		Collection<BlockCrushingRecipe> recipes = recipesByBlock.getOrDefault(
				fallingBlock.getBlock(),
				Collections.EMPTY_LIST
		);
		if (recipes.isEmpty()) {
			return;
		}
		AABB box = entity.getBoundingBox();
		Level level = entity.level();
		BlockState landingBlock = level.getBlockState(pos);
		if (landingBlock.is(LycheeTags.EXTEND_BOX)) {
			box = box.minmax(new AABB(pos));
		}
		List<ItemEntity> itemEntities = entity.level().getEntitiesOfClass(ItemEntity.class, box, $ -> {
			return $.isAlive() && validItems.contains($.getItem());
		});
		BlockCrushingContext.Builder ctxBuilder = new BlockCrushingContext.Builder(
				entity.level(),
				itemEntities,
				entity
		);
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
						if (ctx.getMatch() != null && ctx.getMatch().inputUsed.length > 0) {
							int[] inputUsed = ctx.getMatch().inputUsed;
							//System.out.println(Arrays.toString(ctx.match));
							times = recipe.getRandomRepeats(Integer.MAX_VALUE, ctx);
							for (int i = 0; i < inputUsed.length; i++) {
								if (inputUsed[i] > 0) {
									ItemStack stack = ctx.filteredItems.get(i).getItem();
									times = Math.min(times, stack.getCount() / inputUsed[i]);
								}
							}
						}
						match.get().applyPostActions(ctx, times);
						if (!ctx.runtime.doDefault) {
							((LycheeFallingBlockEntity) entity).lychee$cancelDrop();
						}
						if (CommonProxy.hasKiwi) {
							Set<ItemStackHolder> alreadySentParticles = Sets.newHashSet();
							for (int i = 0; i < ctx.itemHolders.size(); i++) {
								ItemStackHolder holder = ctx.itemHolders.get(i);
								if (!ctx.itemHolders.ignoreConsumptionFlags.get(i) && !holder.get().isEmpty()) {
									if (holder instanceof ItemStackHolder.Entity &&
										!alreadySentParticles.contains(holder)) {
										alreadySentParticles.add(holder);
										SCustomLevelEventPacket.sendItemParticles(
												holder.get(),
												ctx.serverLevel(),
												((ItemStackHolder.Entity) holder).getEntity().position()
										);
									}
								}
							}
						}
						ctx.totalItems -= ctx.itemHolders.postApply(true, times);
						if (!recipe.getMaxRepeats().isAny()) {
							break major;
						}
						ctx.filteredItems = null;
						ctx.setMatch(null);
						ctx.itemEntities.removeIf($ -> $.getItem().isEmpty());
					}
				} catch (Exception e) {
					Lychee.LOGGER.error("", e);
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
