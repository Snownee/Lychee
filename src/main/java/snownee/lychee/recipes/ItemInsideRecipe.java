package snownee.lychee.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.LycheeCounter;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class ItemInsideRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe {
	protected final BlockPredicate blockPredicate;
	protected final int time;
	protected boolean special;
	protected IngredientCollection ingredients;

	public ItemInsideRecipe(
			LycheeRecipeCommonProperties commonProperties,
			BlockPredicate blockPredicate,
			int time,
			final IngredientCollection ingredients
	) {
		super(commonProperties);
		this.blockPredicate = blockPredicate;
		this.time = time;
		this.ingredients = ingredients;
		onConstructed();
	}

	public int time() {
		return time;
	}

	@Override
	public boolean isSpecial() {
		return special;
	}

	@Override
	public BlockPredicate blockPredicate() {
		return blockPredicate;
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
		if (!BlockPredicateExtensions.isAny(blockPredicate) && !BlockPredicateExtensions.matches(blockPredicate, context)) {
			return false;
		}
		return ItemShapelessRecipeUtils.matches(context, ingredients);
	}


	@Override
	public LycheeRecipeSerializer<ItemInsideRecipe> getSerializer() {
		return RecipeSerializers.ITEM_INSIDE;
	}

	@Override
	public ItemInsideRecipeType getType() {
		return RecipeTypes.ITEM_INSIDE;
	}

	@Override
	public IngredientCollection ingredientCollection() {
		return ingredients;
	}

	public static class Serializer implements LycheeRecipeSerializer<ItemInsideRecipe> {
		public static final MapCodec<ItemInsideRecipe> CODEC = ItemShapelessRecipeUtils.validatedCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
				LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
				BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY)
						.forGetter(ItemInsideRecipe::blockPredicate),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("time", 0).forGetter(ItemInsideRecipe::time),
				IngredientCollection.codec(1, Integer.MAX_VALUE)
						.fieldOf(ITEM_IN)
						.forGetter(ItemInsideRecipe::ingredientCollection)
		).apply(instance, ItemInsideRecipe::new)));

		@Override
		public MapCodec<ItemInsideRecipe> codec() {
			return CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ItemInsideRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						ItemInsideRecipe::commonProperties,
						BlockPredicate.STREAM_CODEC,
						ItemInsideRecipe::blockPredicate,
						ByteBufCodecs.VAR_INT,
						ItemInsideRecipe::time,
						IngredientCollection.STREAM_CODEC,
						ItemInsideRecipe::ingredientCollection,
						ItemInsideRecipe::new
				);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ItemInsideRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
