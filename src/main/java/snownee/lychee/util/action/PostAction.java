package snownee.lychee.util.action;

import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.contextual.ContextualPredicate;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface PostAction extends PostActionDisplay, ContextualPredicate, Contextual {
	MapCodec<PostAction> MAP_CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatchMap(
			PostAction::type,
			PostActionType::codec
	);

	Codec<PostAction> CODEC = MAP_CODEC.codec();

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
				.get()
				.left()
				.orElseThrow());
	}

	default void validate(ILycheeRecipe<?> recipe) {}
}
