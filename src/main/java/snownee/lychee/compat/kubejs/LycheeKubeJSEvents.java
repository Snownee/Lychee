package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;

public interface LycheeKubeJSEvents {

	EventGroup GROUP = EventGroup.of("LycheeEvents");

	TargetedEventHandler<String> CLICKED_INFO_BADGE = GROUP.client("clickedInfoBadge", () -> ClickedInfoBadgeKubeEvent.class)
			.supportsTarget(EventTargetType.STRING)
			.hasResult();
	TargetedEventHandler<String> CUSTOM_ACTION = GROUP.startup("customAction", () -> CustomActionKubeEvent.class)
			.requiredTarget(EventTargetType.STRING)
			.hasResult();
	TargetedEventHandler<String> CUSTOM_CONDITION = GROUP.startup("customCondition", () -> CustomConditionKubeEvent.class)
			.requiredTarget(EventTargetType.STRING)
			.hasResult();
}
