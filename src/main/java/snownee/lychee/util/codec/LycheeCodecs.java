package snownee.lychee.util.codec;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public final class LycheeCodecs {
	private static final NonNullList<?> EMPTY_NON_NULL_LIST = NonNullList.copyOf(List.of());

	public static final Codec<ItemStack> PLAIN_ITEM_STACK_CODEC = Codec.withAlternative(
			ItemStack.OPTIONAL_CODEC,
			BuiltInRegistries.ITEM.holderByNameCodec().xmap(ItemStack::new, ItemStack::getItemHolder));

	public static final MapCodec<ItemStack> FLAT_ITEM_STACK_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemStack::getItemHolder),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch))
			.apply(instance, ItemStack::new));

	public static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(posInstance -> posInstance.group(
			Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
			Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
			Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)).apply(
			posInstance, (x, y, z) -> {
				if (x == 0 && y == 0 && z == 0) {
					return BlockPos.ZERO;
				}
				return new BlockPos(x, y, z);
			}));

	public static <T> Codec<NonNullList<T>> nonNullList(Codec<T> elementCodec) {
		return Codec.withAlternative(NonNullList.codecOf(elementCodec), elementCodec, e -> NonNullList.copyOf(List.of(e)));
	}

	public static <T> Codec<NonNullList<T>> nonNullList(Codec<T> elementCodec, int minSize, int maxSize) {
		return Codec.withAlternative(
				sizeLimit(NonNullList.codecOf(elementCodec), minSize, maxSize),
				elementCodec,
				e -> NonNullList.copyOf(List.of(e)));
	}

	public static <T> NonNullList<T> emptyNonNullList() {
		//noinspection unchecked
		return (NonNullList<T>) EMPTY_NON_NULL_LIST;
	}

	public static <T, L extends List<T>> Codec<L> sizeLimit(Codec<L> listCodec, int minSize, int maxSize) {
		return listCodec.validate(list -> {
			if (list.size() < minSize) {
				return DataResult.error(() -> "List is too short: " + minSize + ", expected range [" + minSize + "-" + maxSize + "]");
			}
			if (list.size() > maxSize) {
				return DataResult.error(() -> "List is too long: " + maxSize + ", expected range [" + minSize + "-" + maxSize + "]");
			}
			return DataResult.success(list);
		});
	}
}
