package snownee.lychee.client.gui;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joml.Vector2ic;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.Rect2i;

public abstract class RenderElement implements ScreenElement, Renderable {

	public static final RenderElement EMPTY = new RenderElement() {
		@Override
		public void render(GuiGraphics graphics) {

		}
	};
	public Rect2i bounds = new Rect2i(0, 0, 16, 16);
	protected int z = 0;

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

	public <T extends RenderElement> T bounds(Rect2i bounds) {
		this.bounds = bounds;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(int x, int y) {
		this.bounds.setX(x);
		this.bounds.setY(y);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(Vector2ic position) {
		at(position.x(), position.y());
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T offset(int x, int y) {
		at(bounds.getX() + x, bounds.getY() + y);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T offset(Vector2ic position) {
		offset(position.x(), position.y());
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T at(int x, int y, int z) {
		this.at(x, y);
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T atZ(int z) {
		this.z = z;
		//noinspection unchecked
		return (T) this;
	}


	public <T extends RenderElement> T withSize(int width, int height) {
		this.bounds.setWidth(width);
		this.bounds.setHeight(height);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends RenderElement> T withSize(Vector2ic size) {
		withSize(size.x(), size.y());
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
		return this.bounds.getWidth();
	}

	public int height() {
		return this.bounds.getHeight();
	}

	public int x() {
		return this.bounds.getX();
	}

	public int y() {
		return this.bounds.getY();
	}

	public int z() {
		return z;
	}

	public void render(GuiGraphics graphics, int x, int y) {
		this.at(x, y).render(graphics);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		render(guiGraphics);
	}
}