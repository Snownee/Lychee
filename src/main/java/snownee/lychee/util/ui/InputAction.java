package snownee.lychee.util.ui;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.util.ClientProxy;

public interface InputAction {
	static MousePressed mousePressed(MouseButtonEvent event) {
		return new MousePressed(event);
	}

	default InputConstants.Key keyMapping() {
		return ClientProxy.getKeyMapping(this);
	}

	default boolean isMouseOver(@Nullable InteractiveRenderElement element) {
		return true;
	}

	record MousePressed(MouseButtonEvent event) implements InputAction {
	}

	static KeyPressed keyPressed(KeyEvent event) {
		return new KeyPressed(event);
	}

	record KeyPressed(KeyEvent event) implements InputAction {
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
