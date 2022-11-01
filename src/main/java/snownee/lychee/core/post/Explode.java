package snownee.lychee.core.post;

import java.util.Locale;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

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
	public void doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Vec3 pos = ctx.getParamOrNull(LootContextParams.ORIGIN);
		pos = pos.add(offset.getX(), offset.getY(), offset.getZ());
		float r = Math.min(radius + step * (Mth.sqrt(times) - 1), radius * 4);
		ctx.getLevel().explode(ctx.getParamOrNull(LootContextParams.THIS_ENTITY), null, null, pos.x, pos.y, pos.z, r, fire, blockInteraction);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		GuiGameElement.of(Items.TNT).render(poseStack, x, y);
	}

	@Override
	public Component getDisplayName() {
		String s = switch (blockInteraction) {
		case NONE -> "none";
		case BREAK -> "break";
		case DESTROY -> "destroy";
		};
		return Component.translatable(LUtil.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(getType())) + "." + s);
	}

	public static class Type extends PostActionType<Explode> {

		@Override
		public Explode fromJson(JsonObject o) {

			BlockPos offset = LUtil.parseOffset(o);
			boolean fire = GsonHelper.getAsBoolean(o, "fire", false);
			String s = GsonHelper.getAsString(o, "block_interaction", "break");
			BlockInteraction blockInteraction = switch (s) {
			case "none" -> BlockInteraction.NONE;
			case "break" -> BlockInteraction.BREAK;
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
				o.addProperty("fire", action.fire);
			if (action.blockInteraction != BlockInteraction.BREAK)
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
