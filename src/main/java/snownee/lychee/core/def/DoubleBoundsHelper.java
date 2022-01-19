package snownee.lychee.core.def;

import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleBoundsHelper {

	public static Doubles fromNetwork(FriendlyByteBuf pBuffer) {
		double min = pBuffer.readDouble();
		double max = pBuffer.readDouble();
		if (Double.isNaN(min) && Double.isNaN(max)) {
			return Doubles.ANY;
		}
		return Doubles.between(Double.isNaN(min) ? null : min, Double.isNaN(max) ? null : max);
	}

	public static void toNetwork(Doubles doubles, FriendlyByteBuf pBuffer) {
		Double min = doubles.getMin();
		if (min == null) {
			min = Double.NaN;
		}
		pBuffer.writeDouble(min);
		Double max = doubles.getMin();
		if (max == null) {
			max = Double.NaN;
		}
		pBuffer.writeDouble(max);
	}

}
