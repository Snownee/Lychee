package snownee.lychee.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record AddItemCooldown(PostActionCommonProperties commonProperties, float seconds) implements PostAction {

	@Override
	public PostActionType<?> type() {
		return PostActionTypes.ADD_ITEM_COOLDOWN;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var player = (Player) lootParamsContext.get(LootContextParams.THIS_ENTITY);
		var item = context.getItem(0);
		player.getCooldowns().addCooldown(item.getItem(), (int) (seconds * 20 * times));
	}

	@Override
	public boolean hidden() {
		return true;
	}

	public static class Type implements PostActionType<AddItemCooldown> {
		public static final Codec<AddItemCooldown> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(AddItemCooldown::commonProperties),
						Codec.FLOAT.fieldOf("s").forGetter(AddItemCooldown::seconds)
				).apply(instance, AddItemCooldown::new));

		@Override
		public @NotNull Codec<AddItemCooldown> codec() {
			return CODEC;
		}
	}
}
