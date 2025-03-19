package snownee.lychee.compat.rv.element;

import net.minecraft.client.renderer.Rect2i;

public interface InfoElementHelper {
	int INFO_SIZE = 8;

	Rect2i INFO_RECT = getInfoRect(4, 25);

	static Rect2i getInfoRect(int x, int y) {
		return new Rect2i(x, y, INFO_SIZE, INFO_SIZE);
	}

	static Rect2i offsetRect(Rect2i rect, int x, int y) {
		return new Rect2i(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
	}
}
