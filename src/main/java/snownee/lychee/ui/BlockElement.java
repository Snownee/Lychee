package snownee.lychee.ui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;

public record BlockElement(
		UIElementCommonProperties commonProperties,
		GameElementProperties gameProperties,
		BlockPredicate block) implements GameElement {
	public static final MapCodec<BlockElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			UIElementCommonProperties.CODEC.forGetter(BlockElement::commonProperties),
			GameElementProperties.CODEC.forGetter(BlockElement::gameProperties),
			BlockPredicateExtensions.CODEC_FOR_TESTING.fieldOf("block").forGetter(BlockElement::block)
	).apply(i, BlockElement::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockElement> STREAM_CODEC = StreamCodec.composite(
			UIElementCommonProperties.STREAM_CODEC,
			BlockElement::commonProperties,
			GameElementProperties.STREAM_CODEC,
			BlockElement::gameProperties,
			BlockPredicate.STREAM_CODEC,
			BlockElement::block,
			BlockElement::new);

	@Override
	public UIElementType<?> type() {
		return UIElementType.BLOCK;
	}
}
