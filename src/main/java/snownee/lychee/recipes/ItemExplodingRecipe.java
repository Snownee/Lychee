package snownee.lychee.recipes;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ItemExplodingRecipe extends LycheeRecipe<LycheeContext> implements Comparable<ItemExplodingRecipe> {
	public static void invoke(
			final ServerLevel level,
			Vec3 center,
			List<Entity> entityList,
			float radius,
			boolean small,
			@Nullable Entity directSource) {
		if (directSource != null && directSource.is(LycheeTags.SKIP_ITEM_EXPLODING)) {
			return;
		}
		final var itemEntities = entityList.stream()
				.filter(it -> it instanceof ItemEntity)
				.map(ItemEntity.class::cast);
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		context.put(LycheeContextKey.SMALL_EXPLOSION, small);
		var lootParams = context.initLootParams(RecipeTypes.ITEM_EXPLODING);
		lootParams.set(LootContextParams.ORIGIN, center);
		lootParams.set(LootContextParams.EXPLOSION_RADIUS, radius);
		RecipeTypes.ITEM_EXPLODING.process(itemEntities, context);
	}

	protected IngredientCollection ingredients;
	private final boolean allowSmallExplosion;

	public ItemExplodingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			IngredientCollection ingredients,
			boolean allowSmallExplosion
	) {
		super(commonProperties);
		this.ingredients = ingredients;
		this.allowSmallExplosion = allowSmallExplosion;
		onConstructed();
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		if (!allowSmallExplosion() && context.is(LycheeContextKey.SMALL_EXPLOSION)) {
			return false;
		}
		return ItemShapelessRecipeUtils.matches(context, ingredients);
	}

	@Override
	public LycheeRecipeSerializer<ItemExplodingRecipe> getSerializer() {
		return RecipeSerializers.ITEM_EXPLODING;
	}

	@Override
	public LycheeRecipeType<ItemExplodingRecipe> getType() {
		return RecipeTypes.ITEM_EXPLODING;
	}

	@Override
	public IngredientCollection ingredientCollection() {
		return ingredients;
	}

	public boolean allowSmallExplosion() {
		return allowSmallExplosion;
	}

	@Override
	public int compareTo(ItemExplodingRecipe that) {
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
		return i;
	}

	public static class Serializer implements LycheeRecipeSerializer<ItemExplodingRecipe> {
		public static final MapCodec<ItemExplodingRecipe> CODEC =
				ItemShapelessRecipeUtils.validatedCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
						IngredientCollection.CODEC
								.optionalFieldOf(ITEM_IN, IngredientCollection.EMPTY)
								.forGetter(ItemExplodingRecipe::ingredientCollection),
						LycheeCodecs.ALLOW_SMALL_EXPLOSION.forGetter(ItemExplodingRecipe::allowSmallExplosion)
				).apply(instance, ItemExplodingRecipe::new)));

		@Override
		public MapCodec<ItemExplodingRecipe> codec() {
			return CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ItemExplodingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						ItemExplodingRecipe::commonProperties,
						IngredientCollection.STREAM_CODEC,
						ItemExplodingRecipe::ingredientCollection,
						ByteBufCodecs.BOOL,
						ItemExplodingRecipe::allowSmallExplosion,
						ItemExplodingRecipe::new
				);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ItemExplodingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
