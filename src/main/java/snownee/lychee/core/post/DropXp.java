package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;

public class DropXp extends PostAction {

	public final int xp;

	public DropXp(int xp) {
		this.xp = xp;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.DROP_XP;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		ExperienceOrb.award(ctx.getServerLevel(), pos, xp * times);
	}

	@Override
	public Component getDisplayName() {
		return ClientProxy.format(CommonProxy.makeDescriptionId("postAction", getType().getRegistryName()), xp);
	}

	public static class Type extends PostActionType<DropXp> {

		@Override
		public DropXp fromJson(JsonObject o) {
			return new DropXp(o.get("xp").getAsInt());
		}

		@Override
		public void toJson(DropXp action, JsonObject o) {
			o.addProperty("xp", action.xp);
		}

		@Override
		public DropXp fromNetwork(FriendlyByteBuf buf) {
			return new DropXp(buf.readVarInt());
		}

		@Override
		public void toNetwork(DropXp action, FriendlyByteBuf buf) {
			buf.writeVarInt(action.xp);
		}

	}

}
