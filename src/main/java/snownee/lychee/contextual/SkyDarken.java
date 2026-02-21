package snownee.lychee.contextual;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;

public record SkyDarken(MinMaxBounds.Ints value, boolean requireSkyLight) implements ContextualCondition {

	@Override
	public ContextualConditionType<SkyDarken> type() {
		return ContextualConditionType.SKY_DARKEN;
	}

	@Override
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		return test(ctx.level()) ? times : 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.from(test(level));
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
