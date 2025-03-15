package snownee.lychee.action;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record AddItemCooldown(PostActionCommonProperties commonProperties, float seconds, Optional<Item> item) implements PostAction {

	@Override
	public PostActionType<?> type() {
		return PostActionTypes.ADD_ITEM_COOLDOWN;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var player = (Player) lootParamsContext.get(LootContextParams.THIS_ENTITY);
		var item = context.getItem(0);
		player.getCooldowns().addCooldown(this.item.orElse(item.getItem()), (int) (seconds * 20 * times));
	}

	@Override
	public boolean hidden() {
		return true;
	}

	public static class Type implements PostActionType<AddItemCooldown> {
		public static final MapCodec<AddItemCooldown> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(AddItemCooldown::commonProperties),
						ExtraCodecs.POSITIVE_FLOAT.fieldOf("s").forGetter(AddItemCooldown::seconds),
						BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item").forGetter(AddItemCooldown::item)
				).apply(instance, AddItemCooldown::new));

		@Override
		public MapCodec<AddItemCooldown> codec() {
			return CODEC;
		}
	}
}
