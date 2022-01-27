package snownee.lychee.core.def;

import com.google.gson.JsonObject;

import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import snownee.lychee.core.contextual.ContextualCondition;
import snownee.lychee.mixin.IntRangeAccess;
import snownee.lychee.mixin.TimeCheckAccess;

public class TimeCheckHelper {

	public static TimeCheck fromJson(JsonObject o) {
		TimeCheck check = (TimeCheck) LootItemConditions.TIME_CHECK.getSerializer().deserialize(o, ContextualCondition.gsonContext);
		TimeCheckAccess access = (TimeCheckAccess) check;
		IntRangeAccess range = (IntRangeAccess) access.getValue();
		NumberProviderHelper.requireConstant(range.getMin());
		NumberProviderHelper.requireConstant(range.getMax());
		return check;
	}

}
