package snownee.lychee.core.contextual;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class DirectionCheck implements ContextualCondition {

	public static final Map<String, DirectionCheck> LOOKUP = Maps.newHashMap();

	static {
		for (Direction direction : Direction.values()) {
			Direction direction2 = direction;
			create(direction2.getName().toLowerCase(Locale.ENGLISH), ctx -> ctx.getParam(LycheeLootContextParams.DIRECTION) == direction2);
		}
		create("sides", ctx -> ctx.getParam(LycheeLootContextParams.DIRECTION).getStepY() == 0);
		create("forward", ctx -> {
			Direction direction = ctx.getParam(LycheeLootContextParams.DIRECTION);
			BlockState state = ctx.getParam(LootContextParams.BLOCK_STATE);
			/* off */
			Direction facing = state
					.getOptionalValue(BlockStateProperties.FACING)
					.or(() -> state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING))
					.or(() -> state.getOptionalValue(BlockStateProperties.VERTICAL_DIRECTION))
					.orElseThrow();
			/* on */
			return direction == facing;
		});
		create("axis", ctx -> {
			Direction direction = ctx.getParam(LycheeLootContextParams.DIRECTION);
			BlockState state = ctx.getParam(LootContextParams.BLOCK_STATE);
			/* off */
			Axis axis = state
					.getOptionalValue(BlockStateProperties.AXIS)
					.or(() -> state.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS))
					.orElseThrow();
			/* on */
			return axis.test(direction);
		});
	}

	public static void create(String name, Predicate<LycheeContext> predicate) {
		LOOKUP.put(name, new DirectionCheck(name, predicate));
	}

	private final String name;
	private final Predicate<LycheeContext> predicate;

	private DirectionCheck(String name, Predicate<LycheeContext> predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	@Override
	public ContextualConditionType<? extends ContextualCondition> getType() {
		return ContextualConditionTypes.DIRECTION;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return predicate.test(ctx) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		Component value = Component.translatable("direction.lychee." + name).withStyle(ChatFormatting.WHITE);
		return Component.translatable(makeDescriptionId(inverted), value);
	}

	public static class Type extends ContextualConditionType<DirectionCheck> {

		@Override
		public DirectionCheck fromJson(JsonObject o) {
			return LOOKUP.get(o.get("direction").getAsString());
		}

		@Override
		public void toJson(DirectionCheck condition, JsonObject o) {
			o.addProperty("direction", condition.name);
		}

		@Override
		public DirectionCheck fromNetwork(FriendlyByteBuf buf) {
			return LOOKUP.get(buf.readUtf());
		}

		@Override
		public void toNetwork(DirectionCheck condition, FriendlyByteBuf buf) {
			buf.writeUtf(condition.name);
		}

	}

}
