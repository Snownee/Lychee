package snownee.lychee.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.advancements.criterion.BlockPredicate;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

public abstract class ExplodingRecipe<C extends LycheeContext> extends LycheeRecipe<C> {
	public static final MapCodec<BlockPredicate> DISPLAY_TNT = LycheeCodecs.optionalInput(
			BlockPredicateExtensions.CODEC_FOR_TESTING,
			"display_tnt",
			"tnt");
	public static final MapCodec<Boolean> ALLOW_SMALL_EXPLOSION = Codec.BOOL.optionalFieldOf("allow_small_explosion", false);

	private final boolean allowSmallExplosion;
	private final BlockPredicate displayTNT;

	protected ExplodingRecipe(LycheeRecipeCommonProperties commonProperties, BlockPredicate displayTNT, boolean allowSmallExplosion) {
		super(commonProperties);
		this.allowSmallExplosion = allowSmallExplosion;
		this.displayTNT = displayTNT;
	}

	public boolean allowSmallExplosion() {
		return allowSmallExplosion;
	}

	public BlockPredicate displayTNT() {
		return displayTNT;
	}
}
