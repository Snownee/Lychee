package snownee.lychee.util.action;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.SerializableType;

public interface PostActionType<T extends PostAction> extends SerializableType<T> {
	MapCodec<PostAction> MAP_CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatchMap(PostAction::type, PostActionType::codec);

	StreamCodec<RegistryFriendlyByteBuf, PostAction> STREAM_CODEC = ByteBufCodecs.registry(LycheeRegistries.POST_ACTION.key()).dispatch(
			PostAction::type,
			PostActionType::streamCodec);

	StreamCodec<RegistryFriendlyByteBuf, List<PostAction>> STREAM_LIST_CODEC = STREAM_CODEC.apply(original ->
			// Error on Eclipse without the generic type (?)
			new StreamCodec<RegistryFriendlyByteBuf, List<PostAction>>() {
				@Override
				public void encode(RegistryFriendlyByteBuf byteBuf, List<PostAction> list) {
					var filtered = list.stream().filter(it -> !it.preventSync()).toList();
					ByteBufCodecs.writeCount(byteBuf, filtered.size(), Integer.MAX_VALUE);
					for (PostAction action : filtered) {
						original.encode(byteBuf, action);
					}
				}

				@Override
				public List<PostAction> decode(RegistryFriendlyByteBuf byteBuf) {
					var size = ByteBufCodecs.readCount(byteBuf, Integer.MAX_VALUE);
					var list = new ArrayList<PostAction>(size);
					for (int i = 0; i < size; i++) {
						var action = original.decode(byteBuf);
						if (!action.preventSync()) {
							list.add(action);
						}
					}
					return list;
				}
			});

	Codec<List<PostAction>> LIST_CODEC = KCodecs.compactList(MAP_CODEC.codec());

	@Override
	default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return ByteBufCodecs.fromCodecWithRegistries(codec().codec());
	}
}
