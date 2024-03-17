package snownee.lychee.util.codec;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.util.ExtraCodecs;

public final class CompactListCodec<E> implements Codec<List<E>> {
	private final Codec<E> singleCodec;
	private final Codec<List<E>> listCodec;

	public CompactListCodec(Codec<E> singleCodec) {
		this(singleCodec, false);
	}

	public CompactListCodec(Codec<E> singleCodec, boolean nonEmpty) {
		this.singleCodec = singleCodec;
		this.listCodec = nonEmpty ? ExtraCodecs.nonEmptyList(singleCodec.listOf()) : singleCodec.listOf();
	}

	@Override
	public <T> DataResult<Pair<List<E>, T>> decode(DynamicOps<T> ops, T input) {
		var singleResult = singleCodec.decode(ops, input);
		if (singleResult.result().isPresent()) {
			return singleResult.map($ -> $.mapFirst(List::of));
		}
		return listCodec.decode(ops, input);
	}

	@Override
	public <T> DataResult<T> encode(List<E> input, DynamicOps<T> ops, T prefix) {
		if (input.size() == 1) {
			return singleCodec.encode(input.get(0), ops, prefix);
		}
		return listCodec.encode(input, ops, prefix);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && getClass() == obj.getClass()) {
			CompactListCodec<?> other = (CompactListCodec<?>) obj;
			return singleCodec.equals(other.singleCodec);
		}
		return false;
	}

	@Override
	public String toString() {
		return "CompactListCodec[" + singleCodec + "]";
	}
}
