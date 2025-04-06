package snownee.lychee.ui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.ui.UIElement;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;


public record ItemElement(UIElementCommonProperties commonProperties, ItemStack itemStack) implements UIElement {
	public static final MapCodec<ItemElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			UIElementCommonProperties.CODEC.forGetter(ItemElement::commonProperties),
			LycheeCodecs.NONEMPTY_ITEM_STACK_MAP_CODEC.forGetter(ItemElement::itemStack)
	).apply(i, ItemElement::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemElement> STREAM_CODEC = StreamCodec.composite(
			UIElementCommonProperties.STREAM_CODEC,
			ItemElement::commonProperties,
			ItemStack.STREAM_CODEC,
			ItemElement::itemStack,
			ItemElement::new);

	@Override
	public UIElementType<?> type() {
		return UIElementType.ITEM;
	}
}
