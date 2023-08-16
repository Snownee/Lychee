package snownee.lychee.client.core.post;

import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.CycleStateProperty;
import snownee.lychee.util.CommonProxy;

public class CycleStatePropertyPostActionRenderer implements PostActionRenderer<CycleStateProperty> {

	@Override
	public void render(CycleStateProperty action, PoseStack poseStack, int x, int y) {
		List<BlockState> states = BlockPredicateHelper.getShowcaseBlockStates(action.block, Set.of(action.property));
		BlockState state = CommonProxy.getCycledItem(states, Blocks.AIR.defaultBlockState(), 1000);
		GuiGameElement.of(state).rotateBlock(30, 225, 0).scale(10).render(poseStack, x, y);
	}

}
