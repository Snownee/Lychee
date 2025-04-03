package snownee.lychee.util;

import net.minecraft.client.renderer.Rect2i;

public class RectExtensions {
	public static Rect2i offsetRect(Rect2i rect, int x, int y) {
		return new Rect2i(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
	}

	public static Rect2i square(int x, int y, int size) {
		return new Rect2i(x, y, size, size);
	}
}
