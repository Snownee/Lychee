package snownee.lychee.ui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.ui.UIElement;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;

@NotNullByDefault
public record ItemElement(UIElementCommonProperties commonProperties, ItemStack itemStack) implements UIElement {
	public static final MapCodec<ItemElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			UIElementCommonProperties.CODEC.forGetter(ItemElement::commonProperties),
			LycheeCodecs.NONEMPTY_ITEM_STACK_MAP_CODEC.forGetter(ItemElement::itemStack)
	).apply(i, ItemElement::new));

	@Override
	public UIElementType<?> type() {
		return UIElementType.ITEM;
	}
}
