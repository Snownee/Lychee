package snownee.lychee.ui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.ui.UIElement;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;


public record SpriteElement(UIElementCommonProperties commonProperties, Identifier id, float scale) implements UIElement {
	public static final MapCodec<SpriteElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			UIElementCommonProperties.CODEC.forGetter(SpriteElement::commonProperties),
			Identifier.CODEC.fieldOf("id").forGetter(SpriteElement::id),
			ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1F).forGetter(SpriteElement::scale)
	).apply(i, SpriteElement::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, SpriteElement> STREAM_CODEC = StreamCodec.composite(
			UIElementCommonProperties.STREAM_CODEC,
			SpriteElement::commonProperties,
			Identifier.STREAM_CODEC,
			SpriteElement::id,
			ByteBufCodecs.FLOAT,
			SpriteElement::scale,
			SpriteElement::new);

	@Override
	public UIElementType<?> type() {
		return UIElementType.SPRITE;
	}
}
