package snownee.lychee.core.post.input;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.CommonProxy;

public class DamageItem extends PostAction {

	public final int damage;
	public final Reference target;

	public DamageItem(int damage, Reference target) {
		this.damage = damage;
		this.target = target;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.DAMAGE_ITEM;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		IntList indexes = recipe.getItemIndexes(target);
		Entity thisEntity = ctx.getParam(LootContextParams.THIS_ENTITY);
		for (var index : indexes) {
			ItemStack stack = ctx.getItem(index);
			if (!stack.isDamageableItem()) {
				return;
			}
			ctx.itemHolders.ignoreConsumptionFlags.set(index);
			stack = ctx.itemHolders.split(index, 1).get();
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
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	@Override
	public boolean canRepeat() {
		return false;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	public static class Type extends PostActionType<DamageItem> {

		@Override
		public DamageItem fromJson(JsonObject o) {
			return new DamageItem(GsonHelper.getAsInt(o, "damage", 1), Reference.fromJson(o, "target"));
		}

		@Override
		public void toJson(DamageItem action, JsonObject o) {
			if (action.damage != 1) {
				o.addProperty("damage", 1);
			}
			Reference.toJson(action.target, o, "target");
		}

		@Override
		public DamageItem fromNetwork(FriendlyByteBuf buf) {
			return new DamageItem(buf.readVarInt(), Reference.fromNetwork(buf));
		}

		@Override
		public void toNetwork(DamageItem action, FriendlyByteBuf buf) {
			buf.writeVarInt(action.damage);
			Reference.toNetwork(action.target, buf);
		}

	}

}
