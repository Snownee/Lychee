package snownee.lychee.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;


public class BlockExplodingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe {
	protected final BlockPredicate blockPredicate;
	private final boolean allowSmallExplosion;

	public BlockExplodingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			BlockPredicate blockPredicate,
			boolean allowSmallExplosion
	) {
		super(commonProperties);
		this.blockPredicate = blockPredicate;
		this.allowSmallExplosion = allowSmallExplosion;
		onConstructed();
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		if (!allowSmallExplosion() && context.is(LycheeContextKey.SMALL_EXPLOSION)) {
			return false;
		}
		return BlockPredicateExtensions.isAny(blockPredicate()) || BlockPredicateExtensions.matches(blockPredicate(), context);
	}

	@Override
	public BlockPredicate blockPredicate() {
		return blockPredicate;
	}

	public boolean allowSmallExplosion() {
		return allowSmallExplosion;
	}

	@Override
	public LycheeRecipeSerializer<BlockExplodingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_EXPLODING;
	}

	@Override
	public LycheeRecipeType<BlockExplodingRecipe> getType() {
		return RecipeTypes.BLOCK_EXPLODING;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockExplodingRecipe> {
		public static final MapCodec<BlockExplodingRecipe> CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(BlockExplodingRecipe::commonProperties),
						BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY)
								.forGetter(BlockExplodingRecipe::blockPredicate),
						LycheeCodecs.ALLOW_SMALL_EXPLOSION.forGetter(BlockExplodingRecipe::allowSmallExplosion)
				).apply(instance, BlockExplodingRecipe::new));

		@Override
		public MapCodec<BlockExplodingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, BlockExplodingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						BlockExplodingRecipe::commonProperties,
						BlockPredicate.STREAM_CODEC,
						BlockExplodingRecipe::blockPredicate,
						ByteBufCodecs.BOOL,
						BlockExplodingRecipe::allowSmallExplosion,
						BlockExplodingRecipe::new
				);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlockExplodingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
