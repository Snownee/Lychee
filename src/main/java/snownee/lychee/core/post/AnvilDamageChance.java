package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.block_crushing.LycheeFallingBlockEntity;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class AnvilDamageChance extends PostAction {

	public final float chance;

	public AnvilDamageChance(float chance) {
		this.chance = chance;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.ANVIL_DAMAGE_CHANCE;
	}

	@Override
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.getParam(LootContextParams.THIS_ENTITY);
		if (entity instanceof LycheeFallingBlockEntity) {
			times = checkConditions(recipe, ctx, times);
			if (times > 0) {
				((LycheeFallingBlockEntity) entity).lychee$anvilDamageChance(chance);
			}
		}
		return true;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	public static class Type extends PostActionType<AnvilDamageChance> {

		@Override
		public AnvilDamageChance fromJson(JsonObject o) {
			return new AnvilDamageChance(o.get("chance").getAsFloat());
		}

		@Override
		public AnvilDamageChance fromNetwork(FriendlyByteBuf buf) {
			return new AnvilDamageChance(buf.readFloat());
		}

		@Override
		public void toNetwork(AnvilDamageChance action, FriendlyByteBuf buf) {
			buf.writeFloat(action.chance);
		}

	}

}
