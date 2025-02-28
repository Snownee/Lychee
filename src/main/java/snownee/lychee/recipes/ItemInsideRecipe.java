package snownee.lychee.recipes;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.kiwi.recipe_.SizedIngredient;
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

public class ItemInsideRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<ItemInsideRecipe> {
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
	public @NotNull RecipeSerializer<ItemInsideRecipe> getSerializer() {
		return RecipeSerializers.ITEM_INSIDE;
	}

	@Override
	public @NotNull RecipeType<ItemInsideRecipe> getType() {
		return RecipeTypes.ITEM_INSIDE;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return ingredients.flattenedIngredients();
	}

	@Override
	public List<SizedIngredient> sizedIngredients() {
		return ingredients.ingredients();
	}

	public static class Serializer implements LycheeRecipeSerializer<ItemInsideRecipe> {
		public static final MapCodec<ItemInsideRecipe> CODEC = ItemShapelessRecipeUtils.validatedCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
				LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
				BlockPredicateExtensions.CODEC.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY)
						.forGetter(ItemInsideRecipe::blockPredicate),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("time", 0).forGetter(ItemInsideRecipe::time),
				IngredientCollection.codec(1, Integer.MAX_VALUE)
						.fieldOf(ITEM_IN)
						.forGetter(it -> it.ingredients)
		).apply(instance, ItemInsideRecipe::new)));

		@Override
		public @NotNull MapCodec<ItemInsideRecipe> codec() {
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
						it -> it.ingredients,
						ItemInsideRecipe::new
				);

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, ItemInsideRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
