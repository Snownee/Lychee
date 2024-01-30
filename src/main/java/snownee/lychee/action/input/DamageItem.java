package snownee.lychee.action.input;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
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
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionByPathHolder;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.recipe.LycheeRecipe;

public record DamageItem(int damage, Reference target) implements PostAction<DamageItem>, PostActionByPathHolder<DamageItem> {
	public final int damage;
	public final Reference target;

	@Override
	public PostActionType<DamageItem> type() {
		return PostActionTypes.DAMAGE_ITEM;
	}

	@Override
	public void doApply(LycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(LycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		IntList indexes = recipe.getItemIndexes(target);
		Entity thisEntity = ctx.getParam(LootContextParams.THIS_ENTITY);
		for (var index : indexes) {
			ItemStack stack = ctx.getItem(index);
			if (!stack.isDamageableItem()) {
				return;
			}
			ctx.itemHolders.ignoreConsumptionFlags.set(index);
			stack = ctx.itemHolders.split(index, 1).itemstack();
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
			if (stack.hurt(
					damage,
					ctx.getRandom(),
					thisEntity instanceof ServerPlayer ? (ServerPlayer) thisEntity : null
			)) {
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
	public boolean hidden() {
		return true;
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public void validate(LycheeRecipe recipe, LycheeRecipe.NBTPatchContext patchContext) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	@Override
	public void loadCatalystsInfo(LycheeRecipe recipe, List<IngredientInfo> ingredients) {
		String key = CommonProxy.makeDescriptionId("postAction", getType().getRegistryName());
		Component component = Component.translatable(key, damage).withStyle(ChatFormatting.YELLOW);
		recipe.getItemIndexes(target).forEach(i -> {
			IngredientInfo info = ingredients.get(i);
			info.addTooltip(component);
			info.isCatalyst = true;
		});
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
