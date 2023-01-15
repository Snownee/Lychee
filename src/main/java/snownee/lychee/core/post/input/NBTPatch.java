package snownee.lychee.core.post.input;

import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.Lychee;
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
	public void preApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Preconditions.checkNotNull(ctx.json);
		try {
			//			Lychee.LOGGER.info(ctx.json);
			patch.apply(ctx.json); // we will never patch the root element here
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
