package snownee.lychee.action.input;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record DamageItem(PostActionCommonProperties commonProperties, int damage, Reference target) implements PostAction {

	@Override
	public PostActionType<DamageItem> type() {
		return PostActionTypes.DAMAGE_ITEM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var indexes = recipe.getItemIndexes(target);
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var thisEntity = lootParamsContext.get(LootContextParams.THIS_ENTITY);
		var itemStackHolders = context.get(LycheeContextKey.ITEM);
		for (var index : indexes) {
			var stackHolder = itemStackHolders.get(index);
			if (!stackHolder.get().isDamageableItem()) {
				return;
			}
			stackHolder.setConsumption(0);
			var itemStack = itemStackHolders.split(index, 1).get();

			// Forge hook
			//		if (thisEntity instanceof LivingEntity) {
			//			damage = stack.getItem().damageItem(stack, damage, (LivingEntity) thisEntity, onBroken);
			//		}

			itemStack.hurtAndBreak(
					damage,
					(ServerLevel) context.get(LycheeContextKey.LEVEL),
					thisEntity instanceof ServerPlayer player ? player : null, (it) -> {
						if (thisEntity instanceof LivingEntity livingEntity) {
							EquipmentSlot hand = null;
							if (livingEntity.getMainHandItem() == itemStack) {
								hand = EquipmentSlot.MAINHAND;
							} else if (livingEntity.getOffhandItem() == itemStack) {
								hand = EquipmentSlot.OFFHAND;
							}
							if (hand != null) {
								livingEntity.onEquippedItemBroken(it, hand);
							}
						}
						var item = itemStack.getItem();
						itemStack.shrink(1);
						if (thisEntity instanceof Player player) {
							player.awardStat(Stats.ITEM_BROKEN.get(item));
						}
						itemStack.setDamageValue(0);
					});
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
	public void validate(ILycheeRecipe<?> recipe) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	public static class Type implements PostActionType<DamageItem> {
		public static final MapCodec<DamageItem> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(DamageItem::commonProperties),
						Codec.INT.optionalFieldOf("damage", 1).forGetter(DamageItem::damage),
						Reference.CODEC.optionalFieldOf("target", Reference.DEFAULT).forGetter(DamageItem::target)
				).apply(instance, DamageItem::new));

		@Override
		public @NotNull MapCodec<DamageItem> codec() {
			return CODEC;
		}
	}
}
