package snownee.lychee.compat.jei.elements;

import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class DrawableSimpleItemStack implements IDrawableStatic {
	private final ItemStack item;
	private final int x;
	private final int y;
	private final float scale;

	public DrawableSimpleItemStack(ItemStack item, int x, int y, float scale) {
		this.item = item;
		this.x = x;
		this.y = y;
		this.scale = scale;
	}

	@Override
	public void draw(
			GuiGraphics guiGraphics,
			int xOffset,
			int yOffset,
			int maskTop,
			int maskBottom,
			int maskLeft,
			int maskRight
	) {
		RenderSystem.enableDepthTest();
		var pose = guiGraphics.pose();
		pose.pushPose();
		pose.translate(x + xOffset + maskLeft, y + yOffset + maskTop, 0);
		pose.scale(scale, scale, 1);
		guiGraphics.renderFakeItem(item, 0, 0);
		pose.popPose();
		RenderSystem.disableBlend();
	}

	@Override
	public int getWidth() {
		return (int) (16 * scale);
	}

	@Override
	public int getHeight() {
		return (int) (16 * scale);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}
}
