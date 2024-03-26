package snownee.lychee.compat.rei;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.DisplayUtils;

public class SideBlockIcon extends RenderElement {

	private final ScreenElement mainIcon;
	private final Supplier<BlockState> blockProvider;

	public SideBlockIcon(ScreenElement mainIcon, Supplier<BlockState> blockProvider) {
		this.mainIcon = mainIcon;
		this.blockProvider = blockProvider;
	}

	@Override
	public void render(GuiGraphics graphics) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(x, y, z);
		ms.scale(.625F, .625F, .625F);
		mainIcon.render(graphics, 0, 0);
		ms.popPose();
		GuiGameElement.of(blockProvider.get())
				.lighting(DisplayUtils.SIDE_ICON_LIGHTING)
				.scale(7)
				.rotateBlock(30, 202.5, 0)
				.at(x + 4, y + 2)
				.render(graphics);
	}

}
