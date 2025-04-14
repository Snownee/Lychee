package snownee.lychee.util.ui;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.ui.BlockElement;
import snownee.lychee.ui.ItemElement;
import snownee.lychee.ui.SpriteElement;
import snownee.lychee.ui.TextElement;
import snownee.lychee.util.SerializableType;

public interface UIElementType<T extends UIElement> extends SerializableType<T> {
	UIElementType<SpriteElement> SPRITE = register("sprite", SpriteElement.CODEC, SpriteElement.STREAM_CODEC);
	UIElementType<ItemElement> ITEM = register("item", ItemElement.CODEC, ItemElement.STREAM_CODEC);
	UIElementType<BlockElement> BLOCK = register("block", BlockElement.CODEC, BlockElement.STREAM_CODEC);
	UIElementType<TextElement> TEXT = register("text", TextElement.CODEC, TextElement.STREAM_CODEC);

	static <T extends UIElement> UIElementType<T> register(String name, MapCodec<T> codec) {
		return register(name, () -> codec);
	}

	static <T extends UIElement> UIElementType<T> register(
			String name,
			MapCodec<T> codec,
			StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		return register(
				name, new UIElementType<>() {
					@Override
					public MapCodec<T> codec() {
						return codec;
					}

					@Override
					public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
						return streamCodec;
					}
				});
	}

	static <T extends UIElement> UIElementType<T> register(String name, UIElementType<T> type) {
		return Registry.register(LycheeRegistries.UI_ELEMENT, ResourceLocation.parse(name), type);
	}

	@Override
	default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return ByteBufCodecs.fromCodecWithRegistries(codec().codec());
	}
}
