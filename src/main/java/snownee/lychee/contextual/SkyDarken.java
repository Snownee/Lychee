package snownee.lychee.contextual;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SkyDarken(MinMaxBounds.Ints value, boolean requireSkyLight, boolean canSeeSky) implements ContextualCondition {

	@Override
	public ContextualConditionType<SkyDarken> type() {
		return ContextualConditionType.SKY_DARKEN;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		final var lootParamsContext = ctx.get(LycheeContextKey.LOOT_PARAMS);
		BlockPos pos = lootParamsContext.getOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = BlockPos.containing(lootParamsContext.get(LootContextParams.ORIGIN));
		}
		return test(ctx.level(), pos) ? times : 0;
	}

	private boolean test(Level level, BlockPos pos) {
		if (requireSkyLight && !level.dimensionType().hasSkyLight()) {
			return false;
		}
		if (canSeeSky && !level.canSeeSky(pos)) {
			return false;
		}
		return value.matches(level.getSkyDarken());
	}

	public static class Type implements ContextualConditionType<SkyDarken> {
		public static final MapCodec<SkyDarken> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MinMaxBounds.Ints.CODEC.fieldOf("value").forGetter(SkyDarken::value),
				Codec.BOOL.optionalFieldOf("require_sky_light", false).forGetter(SkyDarken::requireSkyLight),
				Codec.BOOL.optionalFieldOf("can_see_sky", false).forGetter(SkyDarken::canSeeSky)
		).apply(instance, SkyDarken::new));

		@Override
		public @NotNull MapCodec<SkyDarken> codec() {
			return CODEC;
		}
	}
}
