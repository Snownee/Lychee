package snownee.lychee.util.recipe;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeContextKeys;
import snownee.lychee.contextual.Chance;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class BlockKeyableRecipeType<R extends BlockKeyableRecipe> extends LycheeRecipeType<R> {

	protected final Map<Block, List<RecipeHolder<R>>> recipesByBlock = Maps.newHashMap();
	protected final List<RecipeHolder<R>> anyBlockRecipes = Lists.newLinkedList();
	public boolean extractChance;

	public BlockKeyableRecipeType(String name, Class<R> clazz, @Nullable ContextKeySet contextParamSet) {
		super(name, clazz, contextParamSet);
	}

	@Override
	@MustBeInvokedByOverriders
	public void refreshCache() {
		recipesByBlock.clear();
		anyBlockRecipes.clear();
		super.refreshCache();
		final var multimap = ArrayListMultimap.<Block, RecipeHolder<R>>create();
		for (final var recipe : recipes) {
			var iterator = recipe.value().conditions().iterator();
			if (iterator.hasNext()) {
				final var condition = iterator.next();
				if (condition instanceof Chance(float chance) && recipe.value() instanceof ChanceRecipe chanceRecipe) {
					chanceRecipe.setChance(chance);
				}
			}
			if (BlockPredicateExtensions.isAny(recipe.value().blockPredicate())) {
				anyBlockRecipes.add(recipe);
				continue;
			}
			for (final var block :
					BlockPredicateExtensions.matchedBlocks(recipe.value().blockPredicate())) {
				multimap.put(block, recipe);
			}
		}
		for (final var e : multimap.asMap().entrySet()) {
			recipesByBlock.put(e.getKey(), List.copyOf(e.getValue()));
		}
	}

	@Override
	public Comparator<RecipeHolder<R>> comparator() {
		return Comparator.comparing(
				RecipeHolder::value,
				Comparator.comparing((BlockKeyableRecipe $) -> !BlockPredicateExtensions.isAny($.blockPredicate()))
						.thenComparingInt($ -> $.getIngredients().size())
						.thenComparing($ -> !$.maxRepeats().isAny())
						.thenComparing(Recipe::isSpecial)
						.reversed());
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
		final var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParams.set(LootContextParams.ORIGIN, CommonProxy.clampPos(origin, pos));
		lootParams.set(LootContextParams.THIS_ENTITY, player);
		lootParams.set(LootContextParams.BLOCK_STATE, blockstate);
		lootParams.set(LycheeContextKeys.BLOCK_POS, pos);
		lootParams.validate();
		final var stack = player.getItemInHand(hand);
		final var otherStack = player.getItemInHand(
				hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

		context.put(
				LycheeContextKey.ITEM,
				ItemStackHolderCollection.Inventory.of(context, stack, otherStack)
		);
		final var itemContext = context.get(LycheeContextKey.ITEM);
		final var actionContext = context.get(LycheeContextKey.ACTION);

		final Iterable<RecipeHolder<R>> iterable = mergeAnyBlockRecipes(recipes);
		for (final var recipeHolder : iterable) {
			if (tryMatch(recipeHolder, level, context).isPresent()) {
				context.put(recipeHolder);
				R recipe = recipeHolder.value();
				if (!level.isClientSide() && recipe.tickOrApply(context)) {
					if (recipe.sizedIngredients().size() == 1) {
						itemContext.get(1).setConsumption(0);
					}
					for (int i = 0; i < recipe.sizedIngredients().size(); i++) {
						itemContext.get(i).setConsumption(recipe.sizedIngredients().get(i).count());
					}
					int times;
					if (recipe.maxRepeats() == BoundsExtensions.ONE) {
						times = 1;
					} else {
						times = Integer.MAX_VALUE;
						for (var holder : itemContext) {
							if (holder.getConsumption() == 0) {
								continue;
							}
							int count = holder.get().getCount() / holder.getConsumption();
							if (count == 0) {
								return Optional.empty();
							}
							times = Math.min(times, count);
						}
					}
					times = recipe.getRandomRepeats(Math.max(1, times), context);
					recipe.applyPostActions(context, times);
					itemContext.postApply(!actionContext.avoidDefault, times);
					player.setItemInHand(hand, context.getItem(0));
					player.setItemInHand(
							hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
							context.getItem(1)
					);
				}
				return Optional.of(recipe);
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
		final var iterable = mergeAnyBlockRecipes(recipes);
		for (final var recipe : iterable) {
			if (extractChance) {
				var chance = (ChanceRecipe) recipe.value();
				if (chance.getChance() != 1 && chance.getChance() <= context.get(LycheeContextKey.RANDOM).nextFloat()) {
					continue;
				}
			}
			if (tryMatch(recipe, level, context).isPresent()) {
				context.put(recipe);
				recipe.value().applyPostActions(context, 1);
				return recipe;
			}
		}
		return null;
	}

	public Iterable<RecipeHolder<R>> mergeAnyBlockRecipes(List<RecipeHolder<R>> recipes) {
		if (anyBlockRecipes.isEmpty()) {
			return recipes;
		}
		if (recipes.isEmpty()) {
			return anyBlockRecipes;
		}
		return Iterables.concat(recipes, anyBlockRecipes);
	}

}
