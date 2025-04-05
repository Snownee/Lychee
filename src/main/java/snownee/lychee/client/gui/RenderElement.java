package snownee.lychee.client.gui;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import snownee.lychee.util.ui.UIElementCommonProperties;

public abstract class RenderElement implements ScreenElement, Renderable {

	public Vector2i size = new Vector2i(UIElementCommonProperties.DEFAULT_SIZE, UIElementCommonProperties.DEFAULT_SIZE);

	public Vector2f position = new Vector2f();

	public static RenderElement empty() {
		return new RenderElement() {
			@Override
			public void render(GuiGraphics graphics) {}
		};
	}
	protected float z = 0;

	public static RenderElement create(BiConsumer<GuiGraphics, RenderElement> renderable) {
		return new SimpleRenderElement(renderable);
	}

	public static RenderElement create(Function<RenderElement, ScreenElement> renderable) {
		return new SimpleRenderElement(it -> (graphics, element) -> renderable.apply(element));
	}

	public static RenderElement create(ScreenElement renderable) {
		return new SimpleRenderElement(renderable);
	}

	protected float alpha = 1f;

	public <T extends RenderElement> T at(float x, float y) {
		this.position.set(x, y);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(Vector2fc position) {
		this.position.set(position);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T offset(float x, float y) {
		this.position.add(x, y);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T offset(Vector2fc position) {
		this.position.add(position);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(float x, float y, float z) {
		this.at(x, y);
		this.z = z;
		//noinspection unchecked
		return (T) this;
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
		this.size.set(size);
		//noinspection unchecked
		return (T) this;
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
		graphics.pose().pushPose();
		graphics.pose().translate(offsetX, offsetY, 0);
		render(graphics);
		graphics.pose().popPose();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		render(guiGraphics);
	}
}