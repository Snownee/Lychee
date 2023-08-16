package snownee.lychee.client.core.post;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.PlaceBlock;

public class PlaceBlockPostActionRenderer implements PostActionRenderer<PlaceBlock> {

	@Override
	public void render(PlaceBlock action, PoseStack poseStack, int x, int y) {
		BlockState state = BlockPredicateHelper.anyBlockState(action.block);
		if (state.isAir()) {
			GuiGameElement.of(Items.BARRIER).render(poseStack, x, y);
			return;
		}
		GuiGameElement.of(state).rotateBlock(30, 225, 0).scale(10).render(poseStack, x, y);
	}
}
