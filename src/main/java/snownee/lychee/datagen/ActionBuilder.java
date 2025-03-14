package snownee.lychee.datagen;

import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.RecordBuilder;

import net.minecraft.resources.RegistryOps;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionLike;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

public class ActionBuilder<R extends PostAction> extends ContextualBuilder<ActionBuilder<R>> implements PostActionLike {
	private R action;
	private @Nullable Boolean hidden;

	public ActionBuilder(R action) {
		this.action = action;
	}

	@Contract("-> this")
	public ActionBuilder<R> hide() {
		this.hidden = true;
		return this;
	}

	public R build() {
		if (hidden == null && conditions.isEmpty()) {
			return action;
		}
		RegistryOps<Object> ops = Objects.requireNonNull(LycheeBuilder.registryOps.get());
		RecordBuilder<Object> builder = ops.mapBuilder();
		PostAction.MAP_CODEC.encode(action, ops, builder);
		if (hidden != null) {
			LycheeRecipeCommonProperties.HIDE_CODEC.encode(hidden, ops, builder);
			hidden = null;
		}
		if (!conditions.isEmpty()) {
			LycheeRecipeCommonProperties.CONTEXTUAL_CODEC.encode(contextualHolder(), ops, builder);
			conditions.clear();
		}
		//noinspection unchecked
		return action = (R) PostAction.MAP_CODEC.compressedDecode(ops, builder.build(ops.empty()).getOrThrow()).getOrThrow();
	}

	@Override
	public PostAction asAction() {
		return build();
	}
}
