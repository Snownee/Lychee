package snownee.lychee.ui;

import java.util.function.Function;

import org.joml.Vector3fc;

import net.minecraft.world.phys.Vec3;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;

@FunctionalInterface
public interface GameElementRenderer<T extends GameElement> extends Function<T, RenderElement> {
	@Override
	default RenderElement apply(T element) {
		GameElementProperties properties = element.gameProperties();
		Vector3fc localPos = properties.localPos();
		Vector3fc rotationOffset = properties.rotationOffset();
		Vector3fc rotation = properties.rotation();
		return decorated(element)
				.atLocal(localPos.x(), localPos.y(), localPos.z())
				.withRotationOffset(new Vec3(rotationOffset.x(), rotationOffset.y(), rotationOffset.z()))
				.rotate(rotation.x(), rotation.y(), rotation.z())
				.scale(properties.scale());
	}

	GuiGameElement.GuiRenderBuilder decorated(T element);
}
