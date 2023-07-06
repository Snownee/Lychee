package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public interface LycheeKubeJSEvents {

	EventGroup GROUP = EventGroup.of("LycheeEvents");

	EventHandler CLICKED_INFO_BADGE = GROUP.client("clickedInfoBadge", () -> ClickedInfoBadgeEventJS.class).extra(Extra.ID).hasResult();
	EventHandler CUSTOM_ACTION = GROUP.startup("customAction", () -> CustomActionEventJS.class).extra(Extra.STRING).hasResult();
	EventHandler CUSTOM_CONDITION = GROUP.startup("customCondition", () -> CustomConditionEventJS.class).extra(Extra.STRING).hasResult();
}
