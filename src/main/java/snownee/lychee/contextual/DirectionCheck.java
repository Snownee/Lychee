package snownee.lychee.contextual;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public class DirectionCheck implements ContextualCondition<DirectionCheck> {
	public static final Map<String, DirectionCheck> LOOKUPS = Maps.newHashMap();

	static {
		for (final var direction : Direction.values()) {
			createLookup(
					direction.getName().toLowerCase(Locale.ENGLISH),
					ctx -> ctx.get(LycheeLootContextParams.DIRECTION) == direction
			);
		}
		createLookup("sides", ctx -> ctx.get(LycheeLootContextParams.DIRECTION).getStepY() == 0);
		createLookup("forward", ctx -> {
			final var direction = ctx.get(LycheeLootContextParams.DIRECTION);
			final var state = ctx.get(LootContextParams.BLOCK_STATE);
			final var facing = state.getOptionalValue(BlockStateProperties.FACING)
									.or(() -> state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING))
									.or(() -> state.getOptionalValue(BlockStateProperties.VERTICAL_DIRECTION))
									.orElseThrow();
			return direction == facing;
		});
		createLookup("axis", ctx -> {
			final var direction = ctx.get(LycheeLootContextParams.DIRECTION);
			final var state = ctx.get(LootContextParams.BLOCK_STATE);
			final var axis = state.getOptionalValue(BlockStateProperties.AXIS)
								  .or(() -> state.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS))
								  .orElseThrow();
			return axis.test(direction);
		});
	}

	public static void createLookup(String name, Predicate<LycheeRecipeContext> predicate) {
		LOOKUPS.put(name, new DirectionCheck(name, predicate));
	}

	private final String name;
	private final Predicate<LycheeRecipeContext> predicate;

	private DirectionCheck(String name, Predicate<LycheeRecipeContext> predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	@Override
	public ContextualConditionType<DirectionCheck> type() {
		return ContextualConditionTypes.DIRECTION;
	}

	@Override
	public int test(RecipeHolder<LycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		return predicate.test(ctx) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		final var value = Component.translatable("direction.lychee." + name).withStyle(ChatFormatting.WHITE);
		return Component.translatable(getDescriptionId(inverted), value);
	}

	public static class Type implements ContextualConditionType<DirectionCheck> {
		public static final Codec<DirectionCheck> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(Codec.STRING.fieldOf("direction").forGetter(it -> it.name))
				.apply(instance, LOOKUPS::get));

		@Override
		public Codec<DirectionCheck> codec() {
			return CODEC;
		}
	}
}
