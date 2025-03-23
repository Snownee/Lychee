package snownee.lychee.compat.rv.element;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rv.RVs;

public class SideBlockIcon extends RenderElement {

	private final RenderElement mainIcon;
	private final Supplier<BlockState> blockProvider;

	public SideBlockIcon(ScreenElement mainIcon, Supplier<BlockState> blockProvider) {
		this.mainIcon = RenderElement.create(mainIcon).at(0, 0, 100);
		this.blockProvider = blockProvider;
	}

	@Override
	public void render(GuiGraphics graphics) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(x, y, z);
		ms.scale(.625F, .625F, .625F);
		mainIcon.render(graphics);
		ms.popPose();
		GuiGameElement.of(blockProvider.get())
				.lighting(RVs.SIDE_ICON_LIGHTING)
				.scale(7)
				.rotateBlock(30, 202.5, 0)
				.at(x + 4, y + 2)
				.render(graphics);
	}

}
