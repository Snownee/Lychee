package snownee.lychee.category;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.category.UIElement;
import snownee.lychee.util.category.UIElementType;

public record SpriteElement(ResourceLocation id) implements UIElement {
	@Override
	public UIElementType<?> type() {
		return null;
	}

	@NotNullByDefault
	public static class Type implements UIElementType<SpriteElement> {
		public static final MapCodec<SpriteElement> CODEC = null;

		@Override
		public MapCodec<SpriteElement> codec() {
			return CODEC;
		}
	}
}
