package snownee.lychee.recipes;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class LightningChannelingRecipe extends LycheeRecipe<LycheeContext> {
	public static void invoke(final LightningBolt lightningBolt, final List<Entity> entities) {
		var itemEntities = entities.stream().filter(it -> it instanceof ItemEntity).map(ItemEntity.class::cast);
		var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, lightningBolt.level());
		var lootParams = context.initLootParams(RecipeTypes.LIGHTNING_CHANNELING);
		lootParams.set(LootContextParams.ORIGIN, lightningBolt.position());
		lootParams.set(LootContextParams.THIS_ENTITY, lightningBolt);
		RecipeTypes.LIGHTNING_CHANNELING.process(itemEntities, context);
	}

	protected IngredientCollection ingredients;

	@SuppressWarnings("UnreachableCode")
	public LightningChannelingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			final IngredientCollection ingredients
	) {
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
		return ItemShapelessRecipeUtils.matches(context, ingredients);
	}

	@Override
	public LycheeRecipeSerializer<LightningChannelingRecipe> getSerializer() {
		return RecipeSerializers.LIGHTNING_CHANNELING;
	}

	@Override
	public LycheeRecipeType<LightningChannelingRecipe> getType() {
		return RecipeTypes.LIGHTNING_CHANNELING;
	}

	public static class Serializer implements LycheeRecipeSerializer<LightningChannelingRecipe> {
		public static final MapCodec<LightningChannelingRecipe> CODEC =
				ItemShapelessRecipeUtils.validatedCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
						IngredientCollection.CODEC
								.optionalFieldOf(ITEM_IN, IngredientCollection.EMPTY)
								.forGetter(LightningChannelingRecipe::ingredientCollection)
				).apply(instance, LightningChannelingRecipe::new)));
		public static final StreamCodec<RegistryFriendlyByteBuf, LightningChannelingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						LightningChannelingRecipe::commonProperties,
						IngredientCollection.STREAM_CODEC,
						LightningChannelingRecipe::ingredientCollection,
						LightningChannelingRecipe::new
				);

		@Override
		public MapCodec<LightningChannelingRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, LightningChannelingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
