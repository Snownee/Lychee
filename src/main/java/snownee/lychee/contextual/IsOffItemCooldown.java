package snownee.lychee.contextual;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record IsOffItemCooldown(Holder<Item> item) implements ContextualCondition {

	@Override
	public ContextualConditionType<IsOffItemCooldown> type() {
		return ContextualConditionType.IS_OFF_ITEM_COOLDOWN;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.get(LycheeContextKey.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		if (entity instanceof Player player && !testCommon(player)) {
			return 0;
		} else {
			return times;
		}
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		if (player == null) {
			return TriState.DEFAULT;
		}
		return TriState.of(testCommon(player));
	}

	private boolean testCommon(Player player) {
		return !player.getCooldowns().isOnCooldown(item.value());
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted);
		MutableComponent item = item().value().getDescription().copy();
		return Component.translatable(key, item.withStyle(ChatFormatting.WHITE));
	}

	public static class Type implements ContextualConditionType<IsOffItemCooldown> {
		public static final MapCodec<IsOffItemCooldown> CODEC = BuiltInRegistries.ITEM.holderByNameCodec().xmap(
				IsOffItemCooldown::new,
				IsOffItemCooldown::item).fieldOf("item");

		public static final StreamCodec<RegistryFriendlyByteBuf, IsOffItemCooldown> STREAM_CODEC =
				ByteBufCodecs.holderRegistry(Registries.ITEM).map(IsOffItemCooldown::new, IsOffItemCooldown::item);

		@Override
		public MapCodec<IsOffItemCooldown> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, IsOffItemCooldown> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
