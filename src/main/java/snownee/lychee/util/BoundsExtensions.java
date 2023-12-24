package snownee.lychee.util;

import java.text.DecimalFormat;
import java.util.Objects;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
			return Component.literal(DECIMAL_FORMAT.format(bounds.min()));
		}
		return Component.literal(DECIMAL_FORMAT.format(bounds.min()) + "~" + DECIMAL_FORMAT.format(bounds.max()));
	}
}
