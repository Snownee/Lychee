package snownee.lychee.core.def;

import com.google.common.base.Preconditions;

import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class NumberProviderHelper {

	public static void requireConstant(NumberProvider numberProvider) {
		if (numberProvider != null) {
			Preconditions.checkArgument(numberProvider.getType() == NumberProviders.CONSTANT);
		}
	}

	public static Integer toConstant(NumberProvider numberProvider) {
		if (numberProvider == null) {
			return null;
		}
		return ((ConstantValue) numberProvider).getInt(null);
	}

	public static ConstantValue fromConstant(Integer integer) {
		if (integer == null) {
			return null;
		}
		return ConstantValue.exactly(integer);
	}

}
