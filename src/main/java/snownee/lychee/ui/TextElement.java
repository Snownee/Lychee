package snownee.lychee.ui;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.util.codec.LycheeStreamCodecs;
import snownee.lychee.util.ui.UIElement;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;

public record TextElement(
		UIElementCommonProperties commonProperties,
		Component text,
		Optional<Component> darkText,
		int color,
		int darkColor,
		boolean shadow,
		boolean centered) implements UIElement {
	public static final int DEFAULT_LIGHT_MODE_COLOR = 0xFF666666;
	public static final int DEFAULT_DARK_MODE_COLOR = 0xFFBBBBBB;
	public static final Codec<Integer> TEXT_COLOR_CODEC = TextColor.CODEC.xmap(TextColor::getValue, TextColor::fromRgb);
	public static final MapCodec<TextElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			UIElementCommonProperties.CODEC.forGetter(TextElement::commonProperties),
			ComponentSerialization.CODEC.fieldOf("text").forGetter(TextElement::text),
			ComponentSerialization.CODEC.optionalFieldOf("dark_text").forGetter(TextElement::darkText),
			TEXT_COLOR_CODEC.optionalFieldOf("color", DEFAULT_LIGHT_MODE_COLOR).forGetter(TextElement::color),
			TEXT_COLOR_CODEC.optionalFieldOf("dark_color", DEFAULT_LIGHT_MODE_COLOR).forGetter(TextElement::darkColor),
			Codec.BOOL.optionalFieldOf("shadow", false).forGetter(TextElement::shadow),
			Codec.BOOL.optionalFieldOf("centered", false).forGetter(TextElement::centered)
	).apply(i, TextElement::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, TextElement> STREAM_CODEC = LycheeStreamCodecs.composite(
			UIElementCommonProperties.STREAM_CODEC,
			TextElement::commonProperties,
			ComponentSerialization.TRUSTED_STREAM_CODEC,
			TextElement::text,
			ByteBufCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC),
			TextElement::darkText,
			ByteBufCodecs.VAR_INT,
			TextElement::color,
			ByteBufCodecs.VAR_INT,
			TextElement::darkColor,
			ByteBufCodecs.BOOL,
			TextElement::shadow,
			ByteBufCodecs.BOOL,
			TextElement::centered,
			TextElement::new);

	@Override
	public UIElementType<?> type() {
		return UIElementType.TEXT;
	}
}
