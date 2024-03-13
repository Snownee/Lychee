package snownee.lychee.client.action;

import java.util.Set;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.action.CycleStateProperty;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class CycleStatePropertyPostActionRenderer implements PostActionRenderer<CycleStateProperty> {

	@Override
	public void render(CycleStateProperty action, GuiGraphics graphics, int x, int y) {
		var states = BlockPredicateExtensions.getShowcaseBlockStates(action.block(), Set.of(action.property()));
		var state = CommonProxy.getCycledItem(states, Blocks.AIR.defaultBlockState(), 1000);
		GuiGameElement.of(state).rotateBlock(30, 225, 0).scale(10).render(graphics, x, y);
	}
}
