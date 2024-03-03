package snownee.lychee.util.recipe;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.contextual.Chance;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class BlockKeyableRecipeType<R extends BlockKeyableRecipe<?>> extends LycheeRecipeType<LycheeContext, R> {

	protected final Map<Block, List<RecipeHolder<R>>> recipesByBlock = Maps.newHashMap();
	protected final List<RecipeHolder<R>> anyBlockRecipes = Lists.newLinkedList();
	public boolean extractChance;

	public BlockKeyableRecipeType(String name, Class<R> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	public void refreshCache() {
		recipesByBlock.clear();
		anyBlockRecipes.clear();
		super.refreshCache();
		final var multimap = HashMultimap.<Block, RecipeHolder<R>>create();
		for (final var recipe : recipes) {
			Iterator<ContextualCondition> iterator = recipe.value().conditions().iterator();
			if (iterator.hasNext()) {
				final var condition = iterator.next();
				if (condition instanceof Chance chance) {
					((ChanceRecipe) recipe.value()).setChance(chance.chance());
				}
			}
			if (recipe.value().blockPredicate().isEmpty()) {
				anyBlockRecipes.add(recipe);
				continue;
			}
			for (final var block :
					BlockPredicateExtensions.matchedBlocks(recipe.value().blockPredicate().orElseThrow())) {
				multimap.put(block, recipe);
			}
		}
		for (final var e : multimap.asMap().entrySet()) {
			final var list = Lists.newArrayList(e.getValue());
			list.sort(null);
			recipesByBlock.put(e.getKey(), list);
		}
	}

	public List<ItemStack> blockKeysToItems() {
		return recipesByBlock.keySet()
				.stream()
				.map(Block::asItem)
				.filter($ -> $ != Items.AIR)
				.sorted(Comparator.comparingInt(Item::getId))
				.map(Item::getDefaultInstance)
				.toList();
	}

	public Optional<R> process(
			Player player,
			InteractionHand hand,
			BlockPos pos,
			Vec3 origin,
			LycheeContext context
	) {
		if (isEmpty()) {
			return Optional.empty();
		}
		final var level = player.level();
		final var blockstate = level.getBlockState(pos);
		final var recipes = recipesByBlock.getOrDefault(blockstate.getBlock(), List.of());
		if (recipes.isEmpty() && anyBlockRecipes.isEmpty()) {
			return Optional.empty();
		}
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.params().put(LootContextParams.ORIGIN, CommonProxy.clampPos(origin, pos));
		lootParamsContext.params().put(LootContextParams.THIS_ENTITY, player);
		lootParamsContext.params().put(LootContextParams.BLOCK_STATE, blockstate);
		lootParamsContext.params().put(LycheeLootContextParams.BLOCK_POS, pos);
		lootParamsContext.validate(contextParamSet);
		final var stack = player.getItemInHand(hand);
		final var otherStack = player.getItemInHand(
				hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

		context.put(
				LycheeContextKey.ITEM,
				ItemStackHolderCollection.Inventory.of(context, stack, otherStack)
		);
		final var itemContext = context.get(LycheeContextKey.ITEM);
		final var actionContext = context.get(LycheeContextKey.ACTION);

		final var iterable = Iterables.concat(recipes, anyBlockRecipes);
		for (final var recipe : iterable) {
			if (tryMatch(recipe, level, context).isPresent()) {
				if (!level.isClientSide && recipe.value().tickOrApply(context)) {
					int times = Math.min(context.getItem(0).getCount(), context.getItem(1).getCount());
					times = recipe.value().getRandomRepeats(Math.max(1, times), context);
					if (recipe.value().getIngredients().size() == 1) {
						itemContext.get(1).setIgnoreConsumption(true);
					}
					recipe.value().applyPostActions(context, times);
					itemContext.postApply(!actionContext.avoidDefault, times);
					player.setItemInHand(hand, context.getItem(0));
					player.setItemInHand(
							hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
							context.getItem(1)
					);
				}
				return Optional.of(recipe.value());
			}
		}
		return Optional.empty();
	}

	public boolean has(Block block) {
		return !anyBlockRecipes.isEmpty() || recipesByBlock.containsKey(block);
	}

	public boolean has(BlockState state) {
		return has(state.getBlock());
	}

	@Nullable
	public RecipeHolder<R> process(
			Level level,
			BlockState state,
			LycheeContext context
	) {
		final var recipes = recipesByBlock.getOrDefault(state.getBlock(), List.of());
		final var iterable = Iterables.concat(recipes, anyBlockRecipes);
		for (final var recipe : iterable) {
			if (extractChance) {
				var chance = (ChanceRecipe) recipe.value();
				if (chance.getChance() != 1 && chance.getChance() <= context.get(LycheeContextKey.RANDOM).nextFloat()) {
					continue;
				}
			}
			if (tryMatch(recipe, level, context).isPresent()) {
				recipe.value().applyPostActions(context, 1);
				return recipe;
			}
		}
		return null;
	}

}
