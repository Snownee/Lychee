package snownee.lychee.core.def;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import snownee.lychee.mixin.DoublesAccess;

public class DoubleBoundsHelper {

	public static Doubles fromNetwork(FriendlyByteBuf pBuffer) {
		double min = pBuffer.readDouble();
		double max = pBuffer.readDouble();
		if (Double.isNaN(min) && Double.isNaN(max)) {
			return Doubles.ANY;
		}
		return DoublesAccess.create(Double.isNaN(min) ? null : min, Double.isNaN(max) ? null : max);
	}

	public static void toNetwork(Doubles doubles, FriendlyByteBuf pBuffer) {
		Double min = doubles.getMin();
		if (min == null) {
			min = Double.NaN;
		}
		pBuffer.writeDouble(min);
		Double max = doubles.getMax();
		if (max == null) {
			max = Double.NaN;
		}
		pBuffer.writeDouble(max);
	}

	public static float random(MinMaxBounds.Doubles doubles, RandomSource random) {
		float min = doubles.getMin() == null ? Float.MIN_VALUE : doubles.getMin().floatValue();
		float max = doubles.getMax() == null ? Float.MAX_VALUE : doubles.getMax().floatValue();
		if (min == max) {
			return min;
		}
		return Mth.randomBetween(random, min, max);
	}

}
