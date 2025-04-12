package snownee.lychee.util.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.util.ClientProxy;

public interface InputAction {
	static MousePressed mousePressed(int button, double mouseX, double mouseY) {
		return new MousePressed(button, mouseX, mouseY);
	}

	static InputAction mousePressed(int button) {
		MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
		Window window = Minecraft.getInstance().getWindow();
		double mouseX = mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
		double mouseY = mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
		return mousePressed(button, mouseX, mouseY);
	}

	default InputConstants.Key keyMapping() {
		return ClientProxy.getKeyMapping(this);
	}

	default boolean isMouseOver(@Nullable InteractiveRenderElement element) {
		return true;
	}

	class MousePressed implements InputAction {
		public final int button;
		public final double mouseX;
		public final double mouseY;

		public MousePressed(int button, double mouseX, double mouseY) {
			this.button = button;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}
	}

	static KeyPressed keyPressed(int keyCode, int scanCode, int modifiers) {
		return new KeyPressed(keyCode, scanCode, modifiers);
	}

	class KeyPressed implements InputAction {
		public final int keyCode;
		public final int scanCode;
		public final int modifiers;

		public KeyPressed(int keyCode, int scanCode, int modifiers) {
			this.keyCode = keyCode;
			this.scanCode = scanCode;
			this.modifiers = modifiers;
		}

		@Override
		public boolean isMouseOver(@Nullable InteractiveRenderElement element) {
			return element != null && element.isHovered();
		}
	}

	record Direct(String name) implements InputAction {
		public static final Direct SHOW_RECIPES = new Direct("show_recipes");
		public static final Direct SHOW_USAGES = new Direct("show_usages");
		public static final Direct FAVORITE = new Direct("favorite");
	}
}
