package snownee.lychee.util.codec;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public class TweakedItemStackResultCodec extends MapCodec<ItemStack> {
	public static final String LYCHEE_NBT_KEY = "lychee:nbt";
	private final MapCodec<ItemStack> original;

	public TweakedItemStackResultCodec(MapCodec<ItemStack> original) {
		this.original = original;
	}

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		return Stream.concat(original.keys(ops), Stream.of(ops.createString(LYCHEE_NBT_KEY)));
	}

	@Override
	public <T> DataResult<ItemStack> decode(DynamicOps<T> ops, MapLike<T> input) {
		DataResult<ItemStack> itemResult = original.decode(ops, input);
		T t;
		if (itemResult.result().isEmpty() || (t = input.get(LYCHEE_NBT_KEY)) == null) {
			return itemResult;
		}
		DataResult<Pair<CompoundTag, T>> nbtResult = TagParser.AS_CODEC.decode(ops, t);
		ItemStack stack = itemResult.result().get().copy();
		if (nbtResult.result().isEmpty()) {
			return nbtResult.map($ -> stack);
		}
		stack.setTag(nbtResult.result().get().getFirst());
		return DataResult.success(stack);
	}

	@Override
	public <T> RecordBuilder<T> encode(ItemStack input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		RecordBuilder<T> builder = original.encode(input, ops, prefix);
		if (input.hasTag()) {
			builder = builder.add(LYCHEE_NBT_KEY, TagParser.AS_CODEC.encodeStart(ops, input.getTag()));
		}
		return builder;
	}

	@Override
	public String toString() {
		return "TweakedItemStackResultCodec[" + original + "]";
	}
}
