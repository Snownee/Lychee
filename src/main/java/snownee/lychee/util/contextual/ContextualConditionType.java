package snownee.lychee.util.contextual;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

public interface ContextualConditionType<T extends ContextualCondition<T>> {
	Codec<T> codec();

	default T fromNetwork(FriendlyByteBuf buf) {
		return buf.readWithCodecTrusted(NbtOps.INSTANCE, codec());
	}

	default void toNetwork(FriendlyByteBuf buf, T condition) {
		buf.writeWithCodec(NbtOps.INSTANCE, codec(), condition);
	}
}
