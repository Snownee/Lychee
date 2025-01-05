package snownee.lychee.recipes;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.mixin.NonNullListAccess;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ItemExplodingRecipe extends LycheeRecipe<LycheeContext> implements Comparable<ItemExplodingRecipe> {
	public static void invoke(final ServerLevel level, double x, double y, double z, List<Entity> entityList, float radius) {
		final var itemEntities = entityList.stream()
				.filter(it -> it instanceof ItemEntity)
				.map(ItemEntity.class::cast);
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.ORIGIN, new Vec3(x, y, z));
		lootParamsContext.setParam(LootContextParams.EXPLOSION_RADIUS, radius);
		RecipeTypes.ITEM_EXPLODING.process(itemEntities, context);
	}

	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public ItemExplodingRecipe(LycheeRecipeCommonProperties commonProperties) {
		super(commonProperties);
		onConstructed();
	}

	public ItemExplodingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			final List<Ingredient> ingredients
	) {
		super(commonProperties);
		this.ingredients = NonNullListAccess.construct(ingredients, null);
		onConstructed();
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return ItemShapelessRecipeUtils.matches(context, ingredients);
	}

	@Override
	public @NotNull RecipeSerializer<ItemExplodingRecipe> getSerializer() {
		return RecipeSerializers.ITEM_EXPLODING;
	}

	@Override
	public @NotNull LycheeRecipeType<ItemExplodingRecipe> getType() {
		return RecipeTypes.ITEM_EXPLODING;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public int compareTo(@NotNull ItemExplodingRecipe that) {
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
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
						KCodecs.compactList(LycheeCodecs.OPTIONAL_INGREDIENT_CODEC).optionalFieldOf(ITEM_IN, List.of())
								.forGetter(it -> it.ingredients)
				).apply(instance, ItemExplodingRecipe::new)));

		@Override
		public @NotNull MapCodec<ItemExplodingRecipe> codec() {
			return CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ItemExplodingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						ItemExplodingRecipe::commonProperties,
						ByteBufCodecs.fromCodecWithRegistries(KCodecs.compactList(LycheeCodecs.OPTIONAL_INGREDIENT_CODEC)),
						ItemExplodingRecipe::getIngredients,
						ItemExplodingRecipe::new
				);

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, ItemExplodingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
