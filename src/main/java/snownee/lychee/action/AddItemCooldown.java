package snownee.lychee.action;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class AddItemCooldown implements PostAction<AddItemCooldown> {

	public final float seconds;

	public AddItemCooldown(float seconds) {
		this.seconds = seconds;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.ADD_ITEM_COOLDOWN;
	}

	@Override
	public void doApply(ILycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		Player player = (Player) ctx.getParam(LootContextParams.THIS_ENTITY);
		ItemStack stack = ctx.getItem(0);
		player.getCooldowns().addCooldown(stack.getItem(), (int) (seconds * 20 * times));
	}

	@Override
	public boolean hidden() {
		return true;
	}

	public static class Type extends PostActionType<AddItemCooldown> {

		@Override
		public AddItemCooldown fromJson(JsonObject o) {
			return new AddItemCooldown(o.get("s").getAsFloat());
		}

		@Override
		public void toJson(AddItemCooldown action, JsonObject o) {
			o.addProperty("s", action.seconds);
		}

		@Override
		public AddItemCooldown fromNetwork(FriendlyByteBuf buf) {
			return new AddItemCooldown(buf.readFloat());
		}

		@Override
		public void toNetwork(AddItemCooldown action, FriendlyByteBuf buf) {
			buf.writeFloat(action.seconds);
		}

	}

}
