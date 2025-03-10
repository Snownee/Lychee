package snownee.lychee.util.category;

import net.minecraft.core.Registry;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.category.SpriteElement;
import snownee.lychee.util.SerializableType;

public interface UIElementType<T extends UIElement> extends SerializableType<T> {
	UIElementType<SpriteElement> SPRITE = register("sprite", new SpriteElement.Type());

	private static <T extends UIElement> UIElementType<T> register(String name, UIElementType<T> type) {
		Registry.register(LycheeRegistries.UI_ELEMENT, Lychee.id(name), type);
		return type;
	}
}
