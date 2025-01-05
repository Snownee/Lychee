package snownee.lychee.recipes;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ChanceRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class RandomBlockTickingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<RandomBlockTickingRecipe>, ChanceRecipe {
	protected float chance = 1;
	protected final BlockPredicate blockPredicate;

	protected RandomBlockTickingRecipe(LycheeRecipeCommonProperties commonProperties, BlockPredicate blockPredicate) {
		super(commonProperties);
		this.blockPredicate = blockPredicate;
		onConstructed();
	}

	@Override
	public BlockPredicate blockPredicate() {
		return blockPredicate;
	}

	@Override
	public void setChance(float chance) {
		this.chance = chance;
	}

	@Override
	public float getChance() {
		return chance;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return BlockPredicateExtensions.matches(blockPredicate, context);
	}

	@Override
	public @NotNull RecipeSerializer<RandomBlockTickingRecipe> getSerializer() {
		return RecipeSerializers.RANDOM_BLOCK_TICKING;
	}

	@Override
	public @NotNull RecipeType<RandomBlockTickingRecipe> getType() {
		return RecipeTypes.RANDOM_BLOCK_TICKING;
	}

	public static class Serializer implements LycheeRecipeSerializer<RandomBlockTickingRecipe> {
		public static final MapCodec<RandomBlockTickingRecipe> CODEC = RecordCodecBuilder.<RandomBlockTickingRecipe>mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(RandomBlockTickingRecipe::commonProperties),
						BlockPredicateExtensions.CODEC.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY)
								.forGetter(RandomBlockTickingRecipe::blockPredicate)).apply(instance, RandomBlockTickingRecipe::new))
				.validate(it -> {
					if (!it.ghost() && BlockPredicateExtensions.isAny(it.blockPredicate())) {
						return DataResult.error(() -> "Wildcard block input is not allowed for this recipe type.");
					}
					return DataResult.success(it);
				});

		@Override
		public @NotNull MapCodec<RandomBlockTickingRecipe> codec() {
			return CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, RandomBlockTickingRecipe> STREAM_CODEC = StreamCodec.composite(
				LycheeRecipeCommonProperties.STREAM_CODEC,
				RandomBlockTickingRecipe::commonProperties,
				BlockPredicate.STREAM_CODEC,
				RandomBlockTickingRecipe::blockPredicate,
				RandomBlockTickingRecipe::new);

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, RandomBlockTickingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
