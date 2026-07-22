package snownee.lychee.util.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.action.If;
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
	@SuppressWarnings("Convert2MethodRef")
	Codec<PostAction> OBJECT_CODEC = Codec.withAlternative(MAP_CODEC.codec(), Codec.lazyInitialized(() -> If.Type.CODEC.codec()));
	Codec<PostAction> CODEC = Codec.withAlternative(
			OBJECT_CODEC, ExtraCodecs.NON_EMPTY_STRING.flatXmap(
					LycheeParser::action,
					action -> DataResult.error(() -> "Encoding shorthand PostAction is not supported")
			));
	Codec<List<PostAction>> LIST_CODEC = KCodecs.compactList(CODEC);

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

	@Override
	default String toJsonString() {
		return GsonHelper.toStableString(((Codec<PostAction>) type().codec())
				.encodeStart(JsonOps.INSTANCE, this)
				.getOrThrow());
	}

	default void validate(ILycheeRecipe<?> recipe) {}

	@Override
	default PostAction asAction() {
		return this;
	}
}
