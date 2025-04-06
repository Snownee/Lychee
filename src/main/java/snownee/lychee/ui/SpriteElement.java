package snownee.lychee.ui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.ui.UIElement;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;

@NotNullByDefault
public record SpriteElement(UIElementCommonProperties commonProperties, ResourceLocation id, float scale) implements UIElement {
	public static final MapCodec<SpriteElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			UIElementCommonProperties.CODEC.forGetter(SpriteElement::commonProperties),
			ResourceLocation.CODEC.fieldOf("id").forGetter(SpriteElement::id),
			ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1F).forGetter(SpriteElement::scale)
	).apply(i, SpriteElement::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, SpriteElement> STREAM_CODEC = StreamCodec.composite(
			UIElementCommonProperties.STREAM_CODEC,
			SpriteElement::commonProperties,
			ResourceLocation.STREAM_CODEC,
			SpriteElement::id,
			ByteBufCodecs.FLOAT,
			SpriteElement::scale,
			SpriteElement::new);

	@Override
	public UIElementType<?> type() {
		return UIElementType.SPRITE;
	}
}
