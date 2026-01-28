package snownee.lychee.util.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.codec.LycheeParser;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.contextual.ContextualPredicate;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface PostAction extends PostActionDisplay, PostActionLike, ContextualPredicate, Contextual {
	MapCodec<PostAction> MAP_CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatchMap(PostAction::type, PostActionType::codec);
	Codec<PostAction> OBJECT_CODEC = MAP_CODEC.codec();
	Codec<PostAction> STRING_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<PostAction, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<String> stringValue = ops.getStringValue(input);
			if (stringValue.isError()) {
				return DataResult.error(stringValue.error().orElseThrow().messageSupplier());
			}
			if (!(ops instanceof RegistryOps<T> registryOps)) {
				return DataResult.error(() -> "Not a registry ops");
			}
			return LycheeParser.action(new LycheeParser.Context(registryOps), stringValue.getOrThrow()).map($ -> Pair.of($, input));
		}

		@Override
		public <T> DataResult<T> encode(PostAction input, DynamicOps<T> ops, T prefix) {
			return DataResult.error(() -> "Encoding shorthand PostAction is not supported");
		}
	};
	Codec<PostAction> CODEC = Codec.withAlternative(OBJECT_CODEC, STRING_CODEC);
	Codec<List<PostAction>> LIST_CODEC = ExtraCodecs.compactListCodec(CODEC);

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

	PostActionCommonProperties commonProperties();

	default Optional<String> getPath() {
		return commonProperties().getPath();
	}

	default void setPath(String path) {
		commonProperties().setPath(path);
	}

	@Override
	default ContextualHolder conditions() {
		return commonProperties().conditions();
	}

	@Override
	default boolean hidden() {
		return commonProperties().hidden();
	}

	PostActionType<?> type();

	void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times);

	@Override
	default Component getDisplayName() {
		return Component.translatable(CommonProxy.makeDescriptionId(
				"postAction",
				LycheeRegistries.POST_ACTION.getKey(type())
		));
	}

	default boolean repeatable() {
		return true;
	}

	default void getUsedPointers(@Nullable ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {}

	default void onFailure(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {}

	@Override
	default int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return conditions().test(recipe, ctx, times);
	}

	default void validate(ILycheeRecipe<?> recipe) {}

	@Override
	default PostAction asAction() {
		return this;
	}
}
