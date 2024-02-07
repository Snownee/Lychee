package snownee.lychee.util.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public class TweakedItemStackCodec implements Codec<ItemStack> {
	public static final String LYCHEE_NBT_KEY = "lychee:nbt";
	private final Codec<ItemStack> original;

	public TweakedItemStackCodec(Codec<ItemStack> original) {
		this.original = original;
	}

	@Override
	public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
		DataResult<Pair<ItemStack, T>> itemResult = original.decode(ops, input);
		if (itemResult.result().isEmpty()) {
			return itemResult;
		}
		DataResult<T> nbtFieldResult = ops.get(input, LYCHEE_NBT_KEY);
		if (nbtFieldResult.result().isEmpty()) {
			return itemResult;
		}
		DataResult<Pair<CompoundTag, T>> nbtResult = TagParser.AS_CODEC.decode(ops, nbtFieldResult.result().get());
		Pair<ItemStack, T> stack = itemResult.result().get();
		if (nbtResult.result().isEmpty()) {
			return nbtResult.map($ -> stack);
		}
		stack.getFirst().getOrCreateTag().merge(nbtResult.result().get().getFirst());
		return itemResult;
	}

	@Override
	public <T> DataResult<T> encode(ItemStack input, DynamicOps<T> ops, T prefix) {
		DataResult<T> originalResult = original.encode(input, ops, prefix);
		if (originalResult.result().isEmpty() || input.getTag() == null) {
			return originalResult;
		}
		DataResult<T> nbtResult = TagParser.AS_CODEC.encodeStart(ops, input.getTag());
		if (nbtResult.result().isEmpty()) {
			return nbtResult;
		}
		return ops.mergeToMap(originalResult.result().get(), ops.createString(LYCHEE_NBT_KEY), nbtResult.result().get());
	}

	@Override
	public String toString() {
		return "TweakedItemStackCodec[" + original + "]";
	}
}
