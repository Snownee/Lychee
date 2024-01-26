package snownee.lychee.core.post;

import java.util.Locale;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.CommonProxy;

public class Explode extends PostAction {

	public final BlockInteraction blockInteraction;
	public final BlockPos offset;
	public final boolean fire;
	public final float radius;
	public final float step;

	public Explode(BlockInteraction blockInteraction, BlockPos offset, boolean fire, float radius, float step) {
		this.blockInteraction = blockInteraction;
		this.offset = offset;
		this.fire = fire;
		this.radius = radius;
		this.step = step;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.EXPLODE;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		pos = pos.add(offset.getX(), offset.getY(), offset.getZ());
		float r = Math.min(radius + step * (Mth.sqrt(times) - 1), radius * 4);
		CommonProxy.explode(this, ctx.getServerLevel(), pos, ctx.getParamOrNull(LootContextParams.THIS_ENTITY), null, null, r);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(CommonProxy.makeDescriptionId("postAction", getType().getRegistryName()) + "." + blockInteraction.name().toLowerCase(Locale.ENGLISH));
	}

	public static class Type extends PostActionType<Explode> {

		@Override
		public Explode fromJson(JsonObject o) {
			BlockPos offset = CommonProxy.parseOffset(o);
			boolean fire = GsonHelper.getAsBoolean(o, "fire", false);
			String s = GsonHelper.getAsString(o, "block_interaction", "destroy");
			BlockInteraction blockInteraction = switch (s) {
				case "none", "keep" -> BlockInteraction.KEEP;
				case "break", "destroy_with_decay" -> BlockInteraction.DESTROY_WITH_DECAY;
				case "destroy" -> BlockInteraction.DESTROY;
				default -> throw new IllegalArgumentException("Unexpected value: " + s);
			};
			float radius = GsonHelper.getAsFloat(o, "radius", 4);
			float radiusStep = GsonHelper.getAsFloat(o, "radius_step", 0.5F);
			return new Explode(blockInteraction, offset, fire, radius, radiusStep);
		}

		@Override
		public void toJson(Explode action, JsonObject o) {
			BlockPos offset = action.offset;
			if (offset.getX() != 0)
				o.addProperty("offsetX", offset.getX());
			if (offset.getY() != 0)
				o.addProperty("offsetY", offset.getY());
			if (offset.getZ() != 0)
				o.addProperty("offsetZ", offset.getX());
			if (action.fire)
				o.addProperty("fire", true);
			if (action.blockInteraction != BlockInteraction.DESTROY)
				o.addProperty("block_interaction", action.blockInteraction.name().toLowerCase(Locale.ENGLISH));
			if (action.radius != 4)
				o.addProperty("radius", action.radius);
			if (action.step != 0.5F)
				o.addProperty("radius_step", action.step);
		}

		@Override
		public Explode fromNetwork(FriendlyByteBuf buf) {
			BlockInteraction blockInteraction = buf.readEnum(BlockInteraction.class);
			BlockPos offset = buf.readBlockPos();
			boolean fire = buf.readBoolean();
			float radius = buf.readFloat();
			float step = buf.readFloat();
			return new Explode(blockInteraction, offset, fire, radius, step);
		}

		@Override
		public void toNetwork(Explode action, FriendlyByteBuf buf) {
			buf.writeEnum(action.blockInteraction);
			buf.writeBlockPos(action.offset);
			buf.writeBoolean(action.fire);
			buf.writeFloat(action.radius);
			buf.writeFloat(action.step);
		}

	}

}
