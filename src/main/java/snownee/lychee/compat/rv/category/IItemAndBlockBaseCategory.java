package snownee.lychee.compat.rv.category;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.compat.rv.element.InfoElementHelper;
import snownee.lychee.util.RectExtensions;

public interface IItemAndBlockBaseCategory extends WithRemoveActionIcon {
    Rect2i INFO_RECT = InfoElementHelper.getInfoRect(8, 32);
    Rect2i INPUT_BLOCK_RECT = RectExtensions.square(30, 35, 20);
    Rect2i METHOD_RECT = RectExtensions.square(30, 12, 20);

    Rect2i inputBlockRect();

    Rect2i methodRect();

    @Override
    default Vector2ic getRemoveActionPosition() {
        return new Vector2i(
            inputBlockRect().getX() + inputBlockRect().getWidth() - 4,
            inputBlockRect().getY() + inputBlockRect().getHeight() - 8
        );
    }
}
