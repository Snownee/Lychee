package snownee.lychee.util.action;

import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualByConditionsHolder;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.OldLycheeRecipe;

public interface PostAction<Action extends PostAction<Action>> extends Contextual<Action>,
																	   ContextualByConditionsHolder<Action>,
																	   PostActionDisplay {
	Codec<PostAction<?>> CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatch(PostAction::type,
																					 PostActionType::codec);

	Optional<String> path();

	void setPath(String path);

	PostActionType<Action> type();

	@Override
	default Codec<Action> codec() {
		return type().codec();
	}

	default void validate(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipe.NBTPatchContext patchContext) {}

	void apply(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times);

	@Override
	default Component getDisplayName() {
		return Component.translatable(CommonProxy.makeDescriptionId("postAction",
																	LycheeRegistries.POST_ACTION.getKey(type())));
	}

	default boolean repeatable() {
		return true;
	}

	default void getUsedPointers(RecipeHolder<OldLycheeRecipe<?>> recipe, Consumer<JsonPointer> consumer) {}

	default void onFailure(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {}

	@Override
	default String toJsonString() {
		return GsonHelper.toStableString(codec().encodeStart(JsonOps.INSTANCE, (Action) this)
												.get()
												.left()
												.orElseThrow());
	}
}