package snownee.lychee.compat.recipeviewer.element;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.recipeviewer.RVs;

public class SideBlockIcon extends RenderElement {

	private final RenderElement mainIcon;
	private final Supplier<BlockState> blockProvider;

	public SideBlockIcon(ScreenElement mainIcon, Supplier<BlockState> blockProvider) {
		this.mainIcon = RenderElement.create(mainIcon).atZ(100);
		this.blockProvider = blockProvider;
	}

	@Override
	public void render(GuiGraphics graphics) {
		var ms = graphics.pose();
		ms.pushMatrix();
		ms.translate(x(), y());
		ms.pushMatrix();
		ms.scale(.625F);
		mainIcon.render(graphics);
		ms.popMatrix();
		GuiGameElement.of(blockProvider.get())
				.lighting(RVs.SIDE_ICON_LIGHTING)
				.withRotationOffset(Vec3.ZERO)
				.scale(7)
				.rotateBlock(30, 202.5, 0)
				.at(7, 7)
				.render(graphics);
		ms.popMatrix();
	}
}
