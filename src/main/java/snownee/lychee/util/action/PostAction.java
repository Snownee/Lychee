package snownee.lychee.util.action;

import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualByCommonHolder;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.LycheeRecipe;

public interface PostAction<Action extends PostAction<Action>> extends Contextual<Action>,
																	   ContextualByCommonHolder<Action>,
																	   PostActionDisplay {
	Codec<PostAction<?>> CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatch(
			PostAction::type,
			PostActionType::codec
	);

	Optional<String> getPath();

	void setPath(String path);

	PostActionType<Action> type();

	default void validate(@Nullable LycheeRecipe<?> recipe, LycheeRecipe.NBTPatchContext patchContext) {}

	void apply(@Nullable LycheeRecipe<?> recipe, LycheeContext ctx, int times);

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

	default void getUsedPointers(@Nullable LycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {}

	default void onFailure(@Nullable LycheeRecipe<?> recipe, LycheeContext ctx, int times) {}

	@Override
	default String toJsonString() {
		return GsonHelper.toStableString(type().codec()
											   .encodeStart(JsonOps.INSTANCE, (Action) this)
											   .get()
											   .left()
											   .orElseThrow());
	}
}
