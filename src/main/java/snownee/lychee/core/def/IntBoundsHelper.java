package snownee.lychee.core.def;

import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.IntRange;
import snownee.lychee.mixin.IntRangeAccess;
import snownee.lychee.mixin.IntsAccess;

public class IntBoundsHelper {

	public static Ints fromNetwork(FriendlyByteBuf pBuffer) {
		int min = pBuffer.readInt();
		int max = pBuffer.readInt();
		if (min == Integer.MAX_VALUE && max == Integer.MIN_VALUE) {
			return Ints.ANY;
		}
		return IntsAccess.create(min == Integer.MAX_VALUE ? null : min, max == Integer.MIN_VALUE ? null : max);
	}

	public static void toNetwork(Ints ints, FriendlyByteBuf pBuffer) {
		Integer min = ints.getMin();
		if (min == null) {
			min = Integer.MAX_VALUE;
		}
		pBuffer.writeInt(min);
		Integer max = ints.getMax();
		if (max == null) {
			max = Integer.MIN_VALUE;
		}
		pBuffer.writeInt(max);
	}

	public static Ints fromIntRange(IntRangeAccess range) {
		return IntsAccess.create(NumberProviderHelper.toConstant(range.getMin()), NumberProviderHelper.toConstant(range.getMax()));
	}

	public static IntRange toIntRange(Ints ints) {
		return IntRangeAccess.create(NumberProviderHelper.fromConstant(ints.getMin()), NumberProviderHelper.fromConstant(ints.getMax()));
	}

}
