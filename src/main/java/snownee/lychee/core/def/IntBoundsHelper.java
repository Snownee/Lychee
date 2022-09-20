package snownee.lychee.core.def;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.IntRange;
import snownee.lychee.mixin.IntRangeAccess;
import snownee.lychee.mixin.IntsAccess;

public class IntBoundsHelper {

	public static final MinMaxBounds.Ints ONE = MinMaxBounds.Ints.exactly(1);

	public static Ints fromNetwork(FriendlyByteBuf pBuffer) {
		int min = pBuffer.readInt();
		int max = pBuffer.readInt();
		if (min == Integer.MAX_VALUE && max == Integer.MIN_VALUE) {
			return Ints.ANY;
		}
		if (min == 1 && max == 1) {
			return ONE;
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

	public static int random(Ints ints, RandomSource random) {
		int min = ints.getMin() == null ? Integer.MIN_VALUE : ints.getMin();
		int max = ints.getMax() == null ? Integer.MAX_VALUE : ints.getMax();
		if (min == max) {
			return min;
		}
		return Mth.randomBetweenInclusive(random, min, max);
	}

}
