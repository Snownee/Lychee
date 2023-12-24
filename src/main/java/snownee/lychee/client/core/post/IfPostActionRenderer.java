package snownee.lychee.client.core.post;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import snownee.lychee.action.If;

public class IfPostActionRenderer implements PostActionRenderer<If> {

	@Override
	public List<Component> getTooltips(If action) {
		List<Component> list = getBaseTooltips(action);
		action.getConsequenceTooltips(list, action.successEntries, "tip.lychee.ifSuccess");
		action.getConsequenceTooltips(list, action.failureEntries, "tip.lychee.ifFailure");
		return list;
	}

	@Override
	public List<Component> getBaseTooltips(If action) {
		return Lists.newArrayList();
	}
}
