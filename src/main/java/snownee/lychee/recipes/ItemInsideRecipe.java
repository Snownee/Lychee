package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.mixin.NonNullListAccess;
import snownee.lychee.util.LycheeCounter;
import snownee.lychee.util.RecipeMatcher;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class ItemInsideRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<ItemInsideRecipe> {
	protected final @Nullable BlockPredicate blockPredicate;
	protected final int time;
	protected boolean special;
	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public ItemInsideRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			final @Nullable BlockPredicate blockPredicate,
			final int time
	) {
		super(commonProperties);
		this.blockPredicate = blockPredicate;
		this.time = time;
	}

	public ItemInsideRecipe(
			LycheeRecipeCommonProperties commonProperties,
			@Nullable BlockPredicate blockPredicate,
			int time,
			final List<Ingredient> ingredients
	) {
		super(commonProperties);
		this.blockPredicate = blockPredicate;
		this.time = time;
		this.ingredients = NonNullListAccess.construct(ingredients, null);
	}

	public int time() {
		return time;
	}

	@Override
	public boolean isSpecial() {
		return special;
	}

	@Override
	public Optional<BlockPredicate> blockPredicate() {
		return Optional.ofNullable(blockPredicate);
	}

	@Override
	public boolean tickOrApply(LycheeContext context) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var counter = (LycheeCounter) lootParamsContext.get(LootContextParams.THIS_ENTITY);
		if (counter.lychee$getCount() >= time) {
			counter.lychee$setRecipeId(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		var itemShapelessContext = context.get(LycheeContextKey.ITEM_SHAPELESS);
		if (itemShapelessContext.totalItems < ingredients.size()) {
			return false;
		}
		if (!BlockPredicateExtensions.matches(blockPredicate, context)) {
			return false;
		}
		var itemEntities = itemShapelessContext.itemEntities.stream()
				.filter(item -> ingredients.stream().anyMatch(it -> it.test(item.getItem())))
				.limit(ItemShapelessRecipeUtils.MAX_INGREDIENTS)
				.toList();
		var items = itemEntities.stream().map(ItemEntity::getItem).toList();
		var counts = items.stream().mapToInt(ItemStack::getCount).toArray();
		var recipeMatcher = RecipeMatcher.findMatches(items, ingredients, counts);
		if (recipeMatcher.isEmpty()) {
			return false;
		}
		itemShapelessContext.filteredItems = itemEntities;
		itemShapelessContext.setMatcher(recipeMatcher.get());
		return true;
	}

	@Override
	public @NotNull RecipeSerializer<ItemInsideRecipe> getSerializer() {
		return RecipeSerializers.ITEM_INSIDE;
	}

	@Override
	public @NotNull RecipeType<ItemInsideRecipe> getType() {
		return RecipeTypes.ITEM_INSIDE;
	}

	@Override
	public int compareTo(@NotNull ItemInsideRecipe that) {
		int i;
		i = Integer.compare(maxRepeats().isAny() ? 1 : 0, that.maxRepeats().isAny() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = -Integer.compare(ingredients.size(), that.ingredients.size());
		if (i != 0) {
			return i;
		}
		i = -Integer.compare(time(), that.time());
		if (i != 0) {
			return i;
		}
		i = Boolean.compare(isSpecial(), that.isSpecial());
		return i;
	}

	public static class Serializer implements LycheeRecipeSerializer<ItemInsideRecipe> {
		public static final Codec<ItemInsideRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				LycheeRecipeCommonProperties.MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
				BlockPredicateExtensions.CODEC.optionalFieldOf(BLOCK_IN, null).forGetter(it -> it.blockPredicate),
				Codec.INT.fieldOf("time").forGetter(ItemInsideRecipe::time),
				new CompactListCodec<>(Ingredient.CODEC_NONEMPTY, true).optionalFieldOf(ITEM_IN, List.of()).forGetter(it -> it.ingredients)
		).apply(instance, ItemInsideRecipe::new));

		@Override
		public @NotNull Codec<ItemInsideRecipe> codec() {
			return CODEC;
		}
	}
}
