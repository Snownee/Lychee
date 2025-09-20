package snownee.lychee.core.post.input;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.ILycheeRecipe;

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
		LivingEntity entity = thisEntity instanceof LivingEntity ? (LivingEntity) thisEntity : null;
		for (var index : indexes) {
			ctx.itemHolders.ignoreConsumptionFlags.set(index);
			ItemStack itemStack = ctx.itemHolders.split(index, 1).get();
			if (entity == null) {
				// maybe use a fake player?
				if (itemStack.hurt(damage, ctx.getRandom(), null)) {
					itemStack.shrink(1);
					itemStack.setDamageValue(0);
				}
			} else if (entity.getMainHandItem() == itemStack) {
				itemStack.hurtAndBreak(damage, entity, $ -> $.broadcastBreakEvent(EquipmentSlot.MAINHAND));
			} else if (entity.getOffhandItem() == itemStack) {
				itemStack.hurtAndBreak(damage, entity, $ -> $.broadcastBreakEvent(EquipmentSlot.OFFHAND));
			} else {
				itemStack.hurtAndBreak(damage, entity, $ -> {});
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
		Preconditions.checkArgument(recipe.getItemIndexes(target).size() > 0, "No target found for %s", target);
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
