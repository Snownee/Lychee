package snownee.lychee.core.def;

import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.network.FriendlyByteBuf;
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

}
