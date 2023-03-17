package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public interface LycheeKubeJSEvents {

	EventGroup GROUP = EventGroup.of("LycheeEvents");

	EventHandler CLICKED_INFO_BADGE = GROUP.client("clickedInfoBadge", () -> ClickedInfoBadgeEventJS.class).extra(Extra.ID).cancelable();
}
