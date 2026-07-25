package snownee.lychee.recipes;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.ChanceRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

public class SculkSpreadingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe, ChanceRecipe {
	protected float chance = 1;
	protected final BlockPredicate blockPredicate;
	protected final IntProvider charge;

	public SculkSpreadingRecipe(LycheeRecipeCommonProperties commonProperties, BlockPredicate blockPredicate, IntProvider charge) {
		super(commonProperties);
		this.blockPredicate = blockPredicate;
		this.charge = charge;
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

	public IntProvider chargeProvider() {
		return charge;
	}

	public int charge(RandomSource random) {
		return charge.sample(random);
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return BlockPredicateExtensions.matches(blockPredicate, context);
	}

	@Override
	public RecipeSerializer<? extends ILycheeRecipe<LycheeContext>> getSerializer() {
		return RecipeSerializers.SCULK_SPREADING;
	}

	@Override
	public BlockKeyableRecipeType<SculkSpreadingRecipe> getType() {
		return RecipeTypes.SCULK_SPREADING;
	}

	public static final MapCodec<SculkSpreadingRecipe> CODEC = RecordCodecBuilder.<SculkSpreadingRecipe>mapCodec(instance -> instance.group(
					LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(SculkSpreadingRecipe::commonProperties),
					BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY)
							.forGetter(SculkSpreadingRecipe::blockPredicate),
					IntProviders.CODEC.optionalFieldOf("charge", ConstantInt.of(1)).forGetter(SculkSpreadingRecipe::chargeProvider)
			).apply(instance, SculkSpreadingRecipe::new))
			.validate(it -> {
				if (!it.ghost() && BlockPredicateExtensions.isAny(it.blockPredicate())) {
					return DataResult.error(() -> "Wildcard block input is not allowed for this recipe type.");
				}
				return DataResult.success(it);
			});

	public static final StreamCodec<RegistryFriendlyByteBuf, SculkSpreadingRecipe> STREAM_CODEC = StreamCodec.composite(
			LycheeRecipeCommonProperties.STREAM_CODEC,
			SculkSpreadingRecipe::commonProperties,
			BlockPredicate.STREAM_CODEC,
			SculkSpreadingRecipe::blockPredicate,
			ByteBufCodecs.fromCodec(IntProviders.CODEC),
			SculkSpreadingRecipe::chargeProvider,
			SculkSpreadingRecipe::new);
}
