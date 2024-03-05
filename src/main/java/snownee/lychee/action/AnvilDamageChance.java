package snownee.lychee.action;

import java.util.Objects;

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

public final class AnvilDamageChance implements PostAction {
	private final PostActionCommonProperties commonProperties;
	private final float chance;

	public AnvilDamageChance(PostActionCommonProperties commonProperties, float chance) {
		this.commonProperties = commonProperties;
		this.chance = chance;
	}

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

	@Override
	public PostActionCommonProperties commonProperties() {return commonProperties;}

	public float chance() {return chance;}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (AnvilDamageChance) obj;
		return Objects.equals(this.commonProperties, that.commonProperties) &&
				Float.floatToIntBits(this.chance) == Float.floatToIntBits(that.chance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commonProperties, chance);
	}

	@Override
	public String toString() {
		return "AnvilDamageChance[" +
				"commonProperties=" + commonProperties + ", " +
				"chance=" + chance + ']';
	}


	public static class Type implements PostActionType<AnvilDamageChance> {
		public static final Codec<AnvilDamageChance> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(AnvilDamageChance::commonProperties),
						Codec.FLOAT.fieldOf("chance").forGetter(AnvilDamageChance::chance)
				).apply(instance, AnvilDamageChance::new));

		@Override
		public Codec<AnvilDamageChance> codec() {
			return CODEC;
		}
	}
}
