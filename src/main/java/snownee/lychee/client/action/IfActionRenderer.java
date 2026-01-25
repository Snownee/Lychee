package snownee.lychee.client.action;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import snownee.lychee.action.If;
import snownee.lychee.util.action.ActionRenderer;

public class IfActionRenderer implements ActionRenderer<If> {

	@Override
	public List<Component> getTooltips(If action, @Nullable Player player) {
		List<Component> list = getBaseTooltips(action, player);
		action.getConsequenceTooltips(list, action.successEntries(), "tip.lychee.ifSuccess");
		action.getConsequenceTooltips(list, action.failureEntries(), "tip.lychee.ifFailure");
		return list;
	}

	@Override
	public List<Component> getBaseTooltips(If action, @Nullable Player player) {
		return Lists.newArrayList();
	}
}
