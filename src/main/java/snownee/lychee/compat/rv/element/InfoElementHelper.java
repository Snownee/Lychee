package snownee.lychee.compat.rv.element;

import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.util.RectExtensions;

public interface InfoElementHelper {
    int INFO_SIZE = 8;

    Rect2i INFO_RECT = getInfoRect(4, 25);

    static Rect2i getInfoRect(int x, int y) {
        return RectExtensions.square(x, y, INFO_SIZE);
    }

}
