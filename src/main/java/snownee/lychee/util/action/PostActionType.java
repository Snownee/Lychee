package snownee.lychee.util.action;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.util.SerializableType;

public interface PostActionType<T extends PostAction> extends SerializableType<T> {
	@Override
	default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return ByteBufCodecs.fromCodecWithRegistries(codec().codec());
	}
}
