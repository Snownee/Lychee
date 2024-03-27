package snownee.lychee.recipes;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockExplodingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<BlockExplodingRecipe> {
	protected final Optional<BlockPredicate> blockPredicate;

	public BlockExplodingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			Optional<BlockPredicate> blockPredicate
	) {
		super(commonProperties);
		this.blockPredicate = blockPredicate;
		onConstructed();
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		return blockPredicate().isEmpty() || BlockPredicateExtensions.matches(blockPredicate().get(), context);
	}

	@Override
	public Optional<BlockPredicate> blockPredicate() {
		return blockPredicate;
	}

	@Override
	public @NotNull RecipeSerializer<BlockExplodingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_EXPLODING;
	}

	@Override
	public @NotNull RecipeType<BlockExplodingRecipe> getType() {
		return RecipeTypes.BLOCK_EXPLODING;
	}

	@Override
	public int compareTo(@NotNull final BlockExplodingRecipe that) {
		int i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(blockPredicate().isEmpty() ? 1 : 0, that.blockPredicate().isEmpty() ? 1 : 0);
		return i;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockExplodingRecipe> {
		public static final Codec<BlockExplodingRecipe> CODEC =
				RecordCodecBuilder.create(instance -> instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(BlockExplodingRecipe::commonProperties),
						ExtraCodecs.strictOptionalField(BlockPredicateExtensions.CODEC, BLOCK_IN)
								.forGetter(BlockExplodingRecipe::blockPredicate)
				).apply(instance, BlockExplodingRecipe::new));

		@Override
		public @NotNull Codec<BlockExplodingRecipe> codec() {
			return CODEC;
		}
	}
}
