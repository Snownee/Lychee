package snownee.lychee.compat.rv.category;

import org.jetbrains.annotations.Contract;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.client.renderer.Rect2i;

public interface IBlockCrushingRecipeCategory extends RenderRemoveInputBlockIcon {
	Rect2i FALLING_BLOCK_RECT = new Rect2i(0, -35, 20, 35);
	Rect2i LANDING_BLOCK_RECT = new Rect2i(0, 0, 20, 20);

	Rect2i landingBlockRect();

	@Contract(value = "-> new", pure = true)
	@Override
	default Vector2ic getRemoveActionPosition() {
		return new Vector2i(
				landingBlockRect().getX() + landingBlockRect().getWidth() - 4,
				landingBlockRect().getY() + landingBlockRect().getHeight() - 8);
	}
}
