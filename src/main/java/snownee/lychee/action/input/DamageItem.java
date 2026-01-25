package snownee.lychee.action.input;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.CommonProxy;
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
		var indexes = Objects.requireNonNull(recipe).getItemIndexes(target);
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		var thisEntity = lootParams.get(LootContextParams.THIS_ENTITY);
		var itemStackHolders = context.get(LycheeContextKey.ITEM);
		ServerLevel level = (ServerLevel) context.level();
		LivingEntity entity = thisEntity instanceof LivingEntity ? (LivingEntity) thisEntity : null;
		for (var index : indexes) {
			itemStackHolders.get(index).setConsumption(0);
			var itemStack = itemStackHolders.split(index, 1).get();
			if (entity != null && entity.getMainHandItem() == itemStack) {
				itemStack.hurtAndBreak(damage, entity, EquipmentSlot.MAINHAND);
			} else if (entity != null && entity.getOffhandItem() == itemStack) {
				itemStack.hurtAndBreak(damage, entity, EquipmentSlot.OFFHAND);
			} else {
				CommonProxy.hurtAndBreak(itemStack, damage, level, entity);
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
	public void validate(ILycheeRecipe<?> recipe) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	@Override
	public SlotDisplay transformRemainder(SlotDisplay itemStack, @Nullable ILycheeRecipe<?> recipe) {
		if (itemStack.isDamageableItem()) {
			ItemStack copy = itemStack.copy();
			copy.setDamageValue(copy.getDamageValue() + damage);
			return copy;
		}
		return ItemStack.EMPTY;
	}

	public static class Type implements PostActionType<DamageItem> {
		public static final MapCodec<DamageItem> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(DamageItem::commonProperties),
						Codec.INT.optionalFieldOf("damage", 1).forGetter(DamageItem::damage),
						Reference.CODEC.optionalFieldOf("target", Reference.DEFAULT).forGetter(DamageItem::target)
				).apply(instance, DamageItem::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, DamageItem> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				DamageItem::commonProperties,
				ByteBufCodecs.VAR_INT,
				DamageItem::damage,
				Reference.STREAM_CODEC,
				DamageItem::target,
				DamageItem::new);

		@Override
		public MapCodec<DamageItem> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, DamageItem> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
