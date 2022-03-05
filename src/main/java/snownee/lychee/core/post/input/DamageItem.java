package snownee.lychee.core.post.input;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public class DamageItem extends PostAction {

	public final int damage;

	public DamageItem(int damage) {
		this.damage = damage;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.DAMAGE_ITEM;
	}

	@Override
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
		return false;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		ItemStack stack;
		Entity thisEntity = ctx.getParam(LootContextParams.THIS_ENTITY);
		if (thisEntity instanceof ItemEntity) {
			stack = ((ItemEntity) thisEntity).getItem();
		} else {
			stack = ctx.getParam(LootContextParams.TOOL);
		}
		if (!stack.isDamageableItem()) {
			return;
		}
		int damage = this.damage;
		LivingEntity living = null;
		InteractionHand hand = null;
		if (thisEntity instanceof LivingEntity) {
			living = (LivingEntity) thisEntity;
			if (living.getMainHandItem() == stack) {
				hand = InteractionHand.MAIN_HAND;
			} else if (living.getOffhandItem() == stack) {
				hand = InteractionHand.OFF_HAND;
			}
		}
		Consumer<LivingEntity> onBroken;
		if (hand == null) {
			onBroken = $ -> {
			};
		} else {
			InteractionHand hand2 = hand;
			onBroken = $ -> $.broadcastBreakEvent(hand2);
		}
		// Forge hook
		//		if (thisEntity instanceof LivingEntity) {
		//			damage = stack.getItem().damageItem(stack, damage, (LivingEntity) thisEntity, onBroken);
		//		}
		if (stack.hurt(damage, ctx.getRandom(), thisEntity instanceof ServerPlayer ? (ServerPlayer) thisEntity : null)) {
			if (thisEntity instanceof LivingEntity) {
				onBroken.accept((LivingEntity) thisEntity);
			}
			Item item = stack.getItem();
			stack.shrink(1);
			if (thisEntity instanceof Player) {
				((Player) thisEntity).awardStat(Stats.ITEM_BROKEN.get(item));
			}
			stack.setDamageValue(0);
		}
	}

	@Override
	public Component getDisplayName() {
		return LUtil.format(LUtil.makeDescriptionId("postAction", getType().getRegistryName()), damage);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	@Override
	public boolean canRepeat() {
		return false;
	}

	public static class Type extends PostActionType<DamageItem> {

		@Override
		public DamageItem fromJson(JsonObject o) {
			return new DamageItem(GsonHelper.getAsInt(o, "damage", 1));
		}

		@Override
		public DamageItem fromNetwork(FriendlyByteBuf buf) {
			return new DamageItem(buf.readVarInt());
		}

		@Override
		public void toNetwork(DamageItem action, FriendlyByteBuf buf) {
			buf.writeVarInt(action.damage);
		}

	}

}
