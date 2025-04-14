package snownee.lychee.util.ui;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.LycheeRegistries;

public interface UIElement {
	Codec<UIElement> CODEC = LycheeRegistries.UI_ELEMENT.byNameCodec().dispatch(UIElement::type, UIElementType::codec);
	StreamCodec<RegistryFriendlyByteBuf, UIElement> STREAM_CODEC = ByteBufCodecs.registry(LycheeRegistries.UI_ELEMENT.key()).dispatch(
			UIElement::type,
			UIElementType::streamCodec);

	UIElementType<?> type();

	UIElementCommonProperties commonProperties();
}
