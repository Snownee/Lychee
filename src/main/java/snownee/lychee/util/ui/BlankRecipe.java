package snownee.lychee.util.ui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;


public class BlankRecipe extends LycheeRecipe<LycheeContext> {
	protected IngredientCollection ingredients;

	protected BlankRecipe(LycheeRecipeCommonProperties commonProperties, IngredientCollection ingredients) {
		super(commonProperties);
		this.ingredients = ingredients;
		onConstructed();
	}

	@Override
	public IngredientCollection ingredientCollection() {
		return ingredients;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return false;
	}

	@Override
	public LycheeRecipeType<BlankRecipe> getType() {
		return RecipeTypes.BLANK;
	}

	@Override
	public LycheeRecipeSerializer<BlankRecipe> getSerializer() {
		return RecipeSerializers.BLANK;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlankRecipe> {
		public static final MapCodec<BlankRecipe> CODEC = ItemShapelessRecipeUtils.validatedCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
				LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
				IngredientCollection.CODEC
						.optionalFieldOf(ITEM_IN, IngredientCollection.EMPTY)
						.forGetter(BlankRecipe::ingredientCollection)
		).apply(instance, BlankRecipe::new)));

		public static final StreamCodec<RegistryFriendlyByteBuf, BlankRecipe> STREAM_CODEC = StreamCodec.composite(
				LycheeRecipeCommonProperties.STREAM_CODEC,
				BlankRecipe::commonProperties,
				IngredientCollection.STREAM_CODEC,
				BlankRecipe::ingredientCollection,
				BlankRecipe::new);

		@Override
		public MapCodec<BlankRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlankRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
