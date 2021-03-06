package snownee.lychee.core.post;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

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
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
		return true;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		ExperienceOrb.award(ctx.getServerLevel(), pos, xp * times);
	}

	@Override
	public Component getDisplayName() {
		return LUtil.format(LUtil.makeDescriptionId("postAction", getType().getRegistryName()), xp);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		//TODO this is not ideal. render xp orb in the future
		GuiGameElement.of(Items.EXPERIENCE_BOTTLE).render(poseStack, x, y);
	}

	public static class Type extends PostActionType<DropXp> {

		@Override
		public DropXp fromJson(JsonObject o) {
			return new DropXp(o.get("xp").getAsInt());
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
