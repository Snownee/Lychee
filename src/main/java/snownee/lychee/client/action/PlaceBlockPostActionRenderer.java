package snownee.lychee.client.action;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class PlaceBlockPostActionRenderer implements PostActionRenderer<PlaceBlock> {

	@Override
	public void render(PlaceBlock action, GuiGraphics graphics, int x, int y) {
		var state = BlockPredicateExtensions.anyBlockState(action.block());
		if (state.isAir()) {
			GuiGameElement.of(Items.BARRIER).render(graphics, x, y);
			return;
		}
		GuiGameElement.of(state).rotateBlock(30, 225, 0).scale(10).render(graphics, x, y);
	}
}
