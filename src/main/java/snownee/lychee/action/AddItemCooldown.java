package snownee.lychee.action;

import java.util.Objects;

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

public final class AddItemCooldown implements PostAction {
	private final PostActionCommonProperties commonProperties;
	private final float seconds;

	public AddItemCooldown(
			PostActionCommonProperties commonProperties,
			float seconds
	) {
		this.commonProperties = commonProperties;
		this.seconds = seconds;
	}

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

	@Override
	public PostActionCommonProperties commonProperties() {return commonProperties;}

	public float seconds() {return seconds;}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (AddItemCooldown) obj;
		return Objects.equals(this.commonProperties, that.commonProperties) &&
				Float.floatToIntBits(this.seconds) == Float.floatToIntBits(that.seconds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commonProperties, seconds);
	}

	@Override
	public String toString() {
		return "AddItemCooldown[" +
				"commonProperties=" + commonProperties + ", " +
				"seconds=" + seconds + ']';
	}


	public static class Type implements PostActionType<AddItemCooldown> {
		public static final Codec<AddItemCooldown> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(AddItemCooldown::commonProperties),
						Codec.FLOAT.fieldOf("s").forGetter(AddItemCooldown::seconds)
				).apply(instance, AddItemCooldown::new));

		@Override
		public Codec<AddItemCooldown> codec() {
			return CODEC;
		}
	}
}
