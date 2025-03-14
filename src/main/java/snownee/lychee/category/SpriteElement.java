package snownee.lychee.category;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.category.UIElement;
import snownee.lychee.util.category.UIElementType;

public record SpriteElement(ResourceLocation id) implements UIElement {
	@Override
	public UIElementType<?> type() {
		return UIElementType.SPRITE;
	}

	@NotNullByDefault
	public static class Type implements UIElementType<SpriteElement> {
		public static final MapCodec<SpriteElement> CODEC = RecordCodecBuilder.mapCodec(i ->
				i.group(
						ResourceLocation.CODEC.fieldOf("id").forGetter(SpriteElement::id)).apply(i, SpriteElement::new
				));

		@Override
		public MapCodec<SpriteElement> codec() {
			return CODEC;
		}
	}
}
