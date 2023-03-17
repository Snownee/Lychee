package snownee.lychee.compat.kubejs;

import java.util.Map;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.MapJS;
import snownee.lychee.core.contextual.CustomCondition;

public class CustomConditionEventJS extends EventJS {

	public final String id;
	public final CustomCondition condition;
	public final Map<?, ?> data;

	public CustomConditionEventJS(String id, CustomCondition condition) {
		this.id = id;
		this.condition = condition;
		this.data = MapJS.of(condition.data);
	}

}
