package snownee.lychee.recipes;

import java.util.Collections;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.LycheeTags;
import snownee.lychee.context.ItemShapelessContext;
import snownee.lychee.context.RecipeContext;
import snownee.lychee.network.SCustomLevelEventPacket;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.LycheeFallingBlockEntity;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ExtendedItemStackHolder;
import snownee.lychee.util.input.ItemStackHolder;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.ValidItemCache;

public class BlockCrushingRecipeType extends BlockKeyableRecipeType<BlockCrushingRecipe> {

	private final ValidItemCache validItems = new ValidItemCache();

	public BlockCrushingRecipeType(
			String name, Class<BlockCrushingRecipe> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
		compactInputs = true;
	}

	public void process(FallingBlockEntity entity) {
		if (isEmpty()) {
			return;
		}
		final var pos = CommonProxy.getOnPos(entity);
		final var fallingBlock = entity.getBlockState();
		final var recipes = recipesByBlock.getOrDefault(fallingBlock.getBlock(), Collections.emptyList());
		if (recipes.isEmpty()) {
			return;
		}
		var box = entity.getBoundingBox();
		final var level = entity.level();
		final var landingBlock = level.getBlockState(pos);
		if (landingBlock.is(LycheeTags.EXTEND_BOX)) {
			box = box.minmax(new AABB(pos));
		}
		final var itemEntities = entity.level().getEntitiesOfClass(
				ItemEntity.class,
				box,
				$ -> $.isAlive() && validItems.contains($.getItem()));
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, entity.level());
		final var itemShapelessContext = new ItemShapelessContext(itemEntities, context);
		context.put(LycheeContextKey.ITEM_SHAPELESS, itemShapelessContext);
		context.put(LycheeContextKey.FALLING_BLOCK_ENTITY, entity);
		final var matcher = itemShapelessContext.getMatcher();

		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.ORIGIN, entity.position());
		lootParamsContext.setParam(LootContextParams.THIS_ENTITY, entity);
		lootParamsContext.setParam(LootContextParams.BLOCK_STATE, landingBlock);
		lootParamsContext.setParam(LycheeLootContextParams.BLOCK_POS, pos);
		lootParamsContext.validate(contextParamSet);

		final var actionContext = context.get(LycheeContextKey.ACTION);

		boolean matchedAny = false;
		var loop = 0;
		major:
		while (true) {
			var matched = false;
			for (final var recipe : recipes) {
				// recipe without ingredients will only run once to prevent dead loop
				if (recipe.value().ingredients.isEmpty() && loop > 0) {
					continue;
				}
				try {
					final var match = tryMatch(recipe, level, context);
					if (match.isPresent()) {
						matchedAny = matched = true;
						var times = 1;
						context.put(LycheeContextKey.RECIPE_ID, new RecipeContext(recipe.id()));
						context.put(LycheeContextKey.RECIPE, recipe.value());
						if (matcher.map(it -> it.inputUsed.length > 0).orElse(false)) {
							final var inputUsed = matcher.get().inputUsed;
							//System.out.println(Arrays.toString(context.match));
							times = recipe.value().getRandomRepeats(Integer.MAX_VALUE, context);
							for (int i = 0; i < inputUsed.length; i++) {
								if (inputUsed[i] > 0) {
									ItemStack stack = itemShapelessContext.filteredItems.get(i).getItem();
									times = Math.min(times, stack.getCount() / inputUsed[i]);
								}
							}
						}
						match.get().value().applyPostActions(context, times);
						if (actionContext.avoidDefault) {
							((LycheeFallingBlockEntity) entity).lychee$cancelDrop();
						}
						final var alreadySentParticles = Sets.newHashSet();
						final var itemContext = context.get(LycheeContextKey.ITEM);
						for (final ExtendedItemStackHolder holder : itemContext) {
							if (!holder.getIgnoreConsumption() && !holder.get().isEmpty()) {
								if (holder.holder() instanceof ItemStackHolder.Entity entityHolder &&
										!alreadySentParticles.contains(holder)) {
									alreadySentParticles.add(holder);
									var position = entityHolder.getEntity().position();
									new SCustomLevelEventPacket(holder.get(), position)
											.sendToAround((ServerLevel) entityHolder.getEntity().level());
								}
							}
						}
						itemShapelessContext.totalItems -= itemContext.postApply(true, times);
						if (!recipe.value().maxRepeats().isAny()) {
							break major;
						}
						itemShapelessContext.filteredItems = null;
						itemShapelessContext.setMatcher(null);
						itemShapelessContext.itemEntities.removeIf($ -> $.getItem().isEmpty());
					}
				} catch (Exception e) {
					Lychee.LOGGER.error("", e);
					break major;
				}
			}
			if (++loop >= 100 || !matched) {
				break;
			}
		}
		if (matchedAny) {
			final var state = level.getBlockState(entity.blockPosition());
			if (!FallingBlock.isFree(state)) {
				entity.setPosRaw(entity.getX(), pos.getY() + 1, entity.getZ());
			}
			((LycheeFallingBlockEntity) entity).lychee$matched();
			itemShapelessContext.itemEntities.forEach($ -> $.setItem($.getItem().copy())); //sync item amount
		}
	}

	@Override
	public void refreshCache() {
		super.refreshCache();
		validItems.refreshCache(recipes);
	}
}
