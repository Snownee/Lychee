package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class MoveTowardsFace extends PostAction {

	public final float factor;

	public MoveTowardsFace(float factor) {
		this.factor = factor;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.MOVE_TOWARDS_FACE;
	}

	@Override
	public void doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		BlockPos pos = ctx.getParamOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = new BlockPos(ctx.getParam(LootContextParams.ORIGIN));
		}
		Vec3 vec = new Vec3(ctx.getParam(LycheeLootContextParams.DIRECTION).step()).scale(factor);
		ctx.setParam(LootContextParams.ORIGIN, vec.add(Vec3.atCenterOf(pos)));
		ctx.removeParam(LycheeLootContextParams.BLOCK_POS);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	public static class Type extends PostActionType<MoveTowardsFace> {

		@Override
		public MoveTowardsFace fromJson(JsonObject o) {
			return new MoveTowardsFace(GsonHelper.getAsFloat(o, "factor", 1));
		}

		@Override
		public void toJson(MoveTowardsFace action, JsonObject o) {
			o.addProperty("factor", action.factor);
		}

		@Override
		public MoveTowardsFace fromNetwork(FriendlyByteBuf buf) {
			return new MoveTowardsFace(buf.readFloat());
		}

		@Override
		public void toNetwork(MoveTowardsFace action, FriendlyByteBuf buf) {
			buf.writeFloat(action.factor);
		}

	}

}
