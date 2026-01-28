package snownee.lychee.util;

import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.SlotDisplayTypes;

public class Displays {
	public static SlotDisplay slot(@Nullable ItemStackTemplate template) {
		return template == null ? emptySlot() : new SlotDisplay.ItemStackSlotDisplay(template);
	}

	public static SlotDisplay slot(TagKey<Item> tagKey) {
		return new SlotDisplay.TagSlotDisplay(tagKey);
	}

	public static SlotDisplay slot(List<ItemStackTemplate> items) {
		return new SlotDisplay.Composite(items.stream().map(Displays::slot).toList());
	}

	public static SlotDisplay emptySlot() {
		return SlotDisplay.Empty.INSTANCE;
	}

	public record WithDamage(SlotDisplay base, int damage) implements SlotDisplay {
		public static final MapCodec<WithDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				SlotDisplay.CODEC.fieldOf("base").forGetter(WithDamage::base),
				Codec.INT.fieldOf("damage").forGetter(WithDamage::damage)
		).apply(instance, WithDamage::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, WithDamage> STREAM_CODEC = StreamCodec.composite(
				SlotDisplay.STREAM_CODEC, WithDamage::base,
				ByteBufCodecs.VAR_INT, WithDamage::damage,
				WithDamage::new);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> builder) {
			if (!(builder instanceof DisplayContentsFactory.ForStacks<T> stacks)) {
				return Stream.empty();
			}
			List<ItemStack> itemStacks = base.resolveForStacks(context);
			if (itemStacks.isEmpty()) {
				return Stream.empty();
			}
			return itemStacks.stream().peek(itemStack -> {
				if (!itemStack.isDamageableItem()) {
					return;
				}
				if (damage > 0) {
					itemStack.setDamageValue(damage);
				} else {
					itemStack.setDamageValue(itemStack.getMaxDamage() - damage);
				}
			}).map(stacks::forStack);
		}

		@Override
		public Type<? extends SlotDisplay> type() {
			return SlotDisplayTypes.WITH_DAMAGE;
		}
	}
}
