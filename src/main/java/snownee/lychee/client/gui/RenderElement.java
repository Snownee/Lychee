package snownee.lychee.client.gui;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.util.ARGB;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.client.SmartKey;
import snownee.lychee.util.ui.UIElementCommonProperties;

public abstract class RenderElement implements ScreenElement, Renderable {

	public static RenderElement empty() {
		return new RenderElement() {
			@Override
			public void render(GuiGraphics graphics) {}
		};
	}

	public Vector2f position = new Vector2f();
	public Vector2i size = new Vector2i(UIElementCommonProperties.DEFAULT_SIZE);
	protected float z = 0;

	public static RenderElement create(BiConsumer<GuiGraphics, RenderElement> renderable) {
		return new SimpleRenderElement(renderable);
	}

	public static RenderElement create(Function<RenderElement, ScreenElement> renderable) {
		return new SimpleRenderElement(it -> (graphics, element) -> renderable.apply(element));
	}

	public static InteractiveRenderElement create(ScreenElement element) {
		return InteractiveRenderElement.create(element);
	}

	protected float alpha = 1f;

	public <T extends RenderElement> T at(float x, float y) {
		this.position.set(x, y);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(Vector2fc position) {
		return at(position.x(), position.y());
	}

	public <T extends RenderElement> T offset(float x, float y) {
		this.position.add(x, y);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T offset(Vector2fc position) {
		return offset(position.x(), position.y());
	}

	public <T extends RenderElement> T at(float x, float y, float z) {
		this.at(x, y);
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(Vector3fc position) {
		return at(position.x(), position.y(), position.z());
	}

	public <T extends RenderElement> T atZ(float z) {
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}


	public <T extends RenderElement> T withSize(int width, int height) {
		this.size.set(width, height);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T withSize(Vector2ic size) {
		return withSize(size.x(), size.y());
	}

	public <T extends RenderElement> T withSize(int size) {
		withSize(size, size);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T withAlpha(float alpha) {
		this.alpha = alpha;
		//noinspection unchecked
		return (T) this;
	}

	public int width() {
		return this.size.x();
	}

	public int height() {
		return this.size.y();
	}

	public float x() {
		return this.position.x();
	}

	public float y() {
		return this.position.y();
	}

	public float z() {
		return z;
	}

	public boolean containsMouse(double mouseX, double mouseY) {
		return mouseX >= x() && mouseY >= y() && mouseX <= x() + width() && mouseY <= y() + height();
	}

	public void render(GuiGraphics graphics, int offsetX, int offsetY) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(offsetX, offsetY);
		render(graphics);
		graphics.pose().popMatrix();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		render(graphics);
	}

	@SuppressWarnings("unchecked")
	public <T extends RenderElement> T debugOutline(GuiGraphics graphics, int color) {
		if (Platform.isProduction() || !SmartKey.hasControlDown()) {
			return (T) this;
		}
		if (ARGB.alpha(color) == 0) {
			color |= 0x88000000;
		}
		graphics.pose().pushMatrix();
//		graphics.pose().translate(0, 0, 1000);
		graphics.renderOutline(Math.round(x()), Math.round(y()), width(), height(), color);
		graphics.pose().pushMatrix();
		return (T) this;
	}
}