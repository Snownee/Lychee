package snownee.lychee.ui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;


public record ItemElement(
		UIElementCommonProperties commonProperties,
		GameElementProperties gameProperties,
		ItemStackTemplate itemStack) implements GameElement {
	public static final MapCodec<ItemElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			UIElementCommonProperties.CODEC.forGetter(ItemElement::commonProperties),
			GameElementProperties.CODEC.forGetter(ItemElement::gameProperties),
			LycheeCodecs.ITEM_STACK_TEMPLATE_MAP_CODEC.forGetter(ItemElement::itemStack)
	).apply(i, ItemElement::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemElement> STREAM_CODEC = StreamCodec.composite(
			UIElementCommonProperties.STREAM_CODEC,
			ItemElement::commonProperties,
			GameElementProperties.STREAM_CODEC,
			ItemElement::gameProperties,
			ItemStackTemplate.STREAM_CODEC,
			ItemElement::itemStack,
			ItemElement::new);

	@Override
	public UIElementType<?> type() {
		return UIElementType.ITEM;
	}
}
