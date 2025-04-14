package snownee.lychee.contextual;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SkyDarken(MinMaxBounds.Ints value, boolean requireSkyLight) implements ContextualCondition {

	@Override
	public ContextualConditionType<SkyDarken> type() {
		return ContextualConditionType.SKY_DARKEN;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return test(ctx.level()) ? times : 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.of(test(level));
	}

	private boolean test(Level level) {
		if (requireSkyLight && !level.dimensionType().hasSkyLight()) {
			return false;
		}
		return value.matches(level.getSkyDarken());
	}

	public static class Type implements ContextualConditionType<SkyDarken> {
		public static final MapCodec<SkyDarken> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				MinMaxBounds.Ints.CODEC.fieldOf("value").forGetter(SkyDarken::value),
				Codec.BOOL.optionalFieldOf("require_sky_light", false).forGetter(SkyDarken::requireSkyLight)
		).apply(instance, SkyDarken::new));

		@Override
		public MapCodec<SkyDarken> codec() {
			return CODEC;
		}
	}
}
