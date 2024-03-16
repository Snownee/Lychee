package snownee.lychee.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.LycheeFallingBlockEntity;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record AnvilDamageChance(PostActionCommonProperties commonProperties, float chance) implements PostAction {

	@Override
	public PostActionType<AnvilDamageChance> type() {
		return PostActionTypes.ANVIL_DAMAGE_CHANCE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var entity = lootParamsContext.get(LootContextParams.THIS_ENTITY);
		if (entity instanceof LycheeFallingBlockEntity fallingBlockEntity) {
			fallingBlockEntity.lychee$anvilDamageChance(chance);
		}
	}

	@Override
	public boolean hidden() {
		return true;
	}

	public static class Type implements PostActionType<AnvilDamageChance> {
		public static final Codec<AnvilDamageChance> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(AnvilDamageChance::commonProperties),
						Codec.FLOAT.fieldOf("chance").forGetter(AnvilDamageChance::chance)
				).apply(instance, AnvilDamageChance::new));

		@Override
		public @NotNull Codec<AnvilDamageChance> codec() {
			return CODEC;
		}
	}
}
