package snownee.lychee.util;

import java.text.DecimalFormat;
import java.util.Objects;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BoundsExtensions {
	public static final MinMaxBounds.Ints ONE = MinMaxBounds.Ints.exactly(1);

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##");

	public static MutableComponent getDescription(MinMaxBounds<?> bounds) {
		if (bounds.isAny()) {
			return Component.literal("*");
		}
		if (bounds.min().isEmpty()) {
			return Component.literal("<=" + DECIMAL_FORMAT.format(bounds.max().orElseThrow()));
		}
		if (bounds.max().isEmpty()) {
			return Component.literal(">=" + DECIMAL_FORMAT.format(bounds.min().orElseThrow()));
		}
		if (Objects.equals(bounds.min().orElseThrow(), bounds.max().orElseThrow())) {
			return Component.literal(DECIMAL_FORMAT.format(bounds.min().get()));
		}
		return Component.literal(DECIMAL_FORMAT.format(bounds.min().get()) + "~" + DECIMAL_FORMAT.format(bounds.max().get()));
	}

	public static int random(MinMaxBounds.Ints ints, RandomSource random) {
		int min = ints.min().orElse(Integer.MIN_VALUE);
		int max = ints.max().orElse(Integer.MAX_VALUE);
		if (min == max) {
			return min;
		}
		return Mth.randomBetweenInclusive(random, min, max);
	}

	public static float random(MinMaxBounds.Doubles doubles, RandomSource random) {
		float min = doubles.min().map(Double::floatValue).orElse(Float.MIN_VALUE);
		float max = doubles.max().map(Double::floatValue).orElse(Float.MAX_VALUE);
		if (min == max) {
			return min;
		}
		return Mth.randomBetween(random, min, max);
	}
}
