package snownee.lychee.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeType;


public class BlockExplodingRecipe extends ExplodingRecipe<LycheeContext> implements BlockKeyableRecipe {
	protected final BlockPredicate blockPredicate;

	public BlockExplodingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			BlockPredicate blockPredicate,
			BlockPredicate displayTNT,
			boolean allowSmallExplosion
	) {
		super(commonProperties, displayTNT, allowSmallExplosion);
		this.blockPredicate = blockPredicate;
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

	@Override
	public RecipeSerializer<? extends ILycheeRecipe<LycheeContext>> getSerializer() {
		return RecipeSerializers.BLOCK_EXPLODING;
	}

	@Override
	public LycheeRecipeType<BlockExplodingRecipe> getType() {
		return RecipeTypes.BLOCK_EXPLODING;
	}

	public static final MapCodec<BlockExplodingRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(BlockExplodingRecipe::commonProperties),
					BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY)
							.forGetter(BlockExplodingRecipe::blockPredicate),
					DISPLAY_TNT.forGetter(ExplodingRecipe::displayTNT),
					ALLOW_SMALL_EXPLOSION.forGetter(ExplodingRecipe::allowSmallExplosion)
			).apply(instance, BlockExplodingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockExplodingRecipe> STREAM_CODEC =
			StreamCodec.composite(
					LycheeRecipeCommonProperties.STREAM_CODEC,
					BlockExplodingRecipe::commonProperties,
					BlockPredicate.STREAM_CODEC,
					BlockExplodingRecipe::blockPredicate,
					BlockPredicate.STREAM_CODEC,
					BlockExplodingRecipe::displayTNT,
					ByteBufCodecs.BOOL,
					BlockExplodingRecipe::allowSmallExplosion,
					BlockExplodingRecipe::new
			);
}
