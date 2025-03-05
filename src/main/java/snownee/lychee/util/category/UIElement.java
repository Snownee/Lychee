package snownee.lychee.util.category;

import com.mojang.serialization.Codec;

import snownee.lychee.LycheeRegistries;

public interface UIElement {
	Codec<UIElement> CODEC = LycheeRegistries.UI_ELEMENT.byNameCodec().dispatch(UIElement::type, UIElementType::codec);

	UIElementType<?> type();
}
