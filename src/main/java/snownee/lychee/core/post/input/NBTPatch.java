package snownee.lychee.core.post.input;

import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeConfig;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.ActionRuntime.State;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.json.JsonPatch;
import snownee.lychee.util.json.JsonPointer;

public class NBTPatch extends PostAction {

	private static final NBTPatch DUMMY = new NBTPatch(null);

	private final JsonPatch patch;

	public NBTPatch(JsonPatch patch) {
		this.patch = patch;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.NBT_PATCH;
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

	@Override
	public void preApply(ILycheeRecipe<?> recipe, LycheeContext ctx, ILycheeRecipe.NBTPatchContext patchContext) {
		Preconditions.checkNotNull(ctx.json);

		JsonPointer from = patch.from;
		if (from != null) {
			from = patchContext.convertPath(from, (first, second) -> {
				return "/" + recipe.getItemIndexes(new JsonPointer(first)).getInt(0) + second;
			});
		}
		JsonPatch patchClone = new JsonPatch(patch.op, patch.path, from, patch.value);

		try {
			if (recipe.isActionPath(patch.path)) {
				patchClone.apply(ctx.json); // we will never patch the root element here
			} else {
				for (Integer index : recipe.getItemIndexes(patchContext.convertPath(patch.path, (first, second) -> first))) {
					patchClone.path = patchContext.convertPath(patch.path, (first, second) -> "/" + index + second);
					patchClone.apply(ctx.json);
					if (LycheeConfig.debug)
						Lychee.LOGGER.info(ctx.json.toString());
				}
			}
		} catch (Exception e) {
			if (patch.op == JsonPatch.Type.test) {
				ctx.runtime.state = State.STOPPED;
			} else {
				Lychee.LOGGER.error("Ctx json: " + ctx.json);
				Lychee.LOGGER.error("Action json: " + toJson());
				throw e;
			}
		}
	}

	@Override
	public void getUsedPointers(ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {
		consumer.accept(patch.path);
		if (patch.from != null) {
			consumer.accept(patch.from);
		}
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		Preconditions.checkArgument(patchContext.countTargets(recipe, patch.path) > 0, "No target found for %s", patch.path);
		if (patch.from != null) {
			int size = patchContext.countTargets(recipe, patch.from);
			Preconditions.checkArgument(size > 0, "No source found for %s", patch.from);
			Preconditions.checkArgument(size == 1, "Ambiguous source for %s", patch.from);
		}
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	public static class Type extends PostActionType<NBTPatch> {

		@Override
		public NBTPatch fromJson(JsonObject o) {
			JsonPatch patch = JsonPatch.parse(o);
			Preconditions.checkNotNull(patch);
			return new NBTPatch(patch);
		}

		@Override
		public void toJson(NBTPatch action, JsonObject o) {
			o.add("patch", action.patch.toJson());
		}

		@Override
		public NBTPatch fromNetwork(FriendlyByteBuf buf) {
			return DUMMY;
		}

		@Override
		public void toNetwork(NBTPatch action, FriendlyByteBuf buf) {
		}

	}

}
