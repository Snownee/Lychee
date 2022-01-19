package snownee.lychee.compat.jei;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;

public class ThrowItemIcon extends RenderElement {

	private final Supplier<BlockState> blockProvider;

	public ThrowItemIcon(Supplier<BlockState> blockProvider) {
		this.blockProvider = blockProvider;
	}

	@Override
	public void render(PoseStack ms) {
		ms.pushPose();
		ms.translate(x, y, z);
		ms.scale(.6F, .6F, .6F);
		AllGuiTextures.JEI_DOWN_ARROW.render(ms, 0, 0);
		ms.popPose();
		GuiGameElement.of(blockProvider.get()).scale(7).rotateBlock(12.5f, 22.5f, 0).at(x + 6, y + 16).render(ms);
	}

}
