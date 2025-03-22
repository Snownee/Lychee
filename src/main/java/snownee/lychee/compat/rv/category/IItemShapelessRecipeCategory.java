package snownee.lychee.compat.rv.category;

import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.compat.rv.element.InfoElementHelper;

public interface IItemShapelessRecipeCategory extends WithRemoveActionIcon {
	Rect2i INFO_RECT = InfoElementHelper.getInfoRect(3, 25);
}
