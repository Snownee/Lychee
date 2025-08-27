package snownee.lychee.action;

import java.util.Collection;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record CycleStateProperty(
		PostActionCommonProperties commonProperties,
		BlockPredicate block,
		String propertyName,
		Supplier<Property<?>> propertySupplier,
		BlockPos offset,
		boolean reversed) implements PostAction {

	public CycleStateProperty(
			PostActionCommonProperties commonProperties,
			BlockPredicate block,
			BlockPos offset,
			String propertyName,
			boolean reversed) {
		this(commonProperties, block, propertyName, Suppliers.memoize(() -> findProperty(block, propertyName)), offset, reversed);
	}

	public static Property<?> findProperty(BlockPredicate blockPredicate, String name) {
		var block = BlockPredicateExtensions.anyBlockState(blockPredicate);
		for (var property : block.getProperties()) {
			if (name.equals(property.getName())) {
				return property;
			}
		}
		throw new IllegalArgumentException("Unknown property name: " + name);
	}

	@Override
	public PostActionType<CycleStateProperty> type() {
		return PostActionTypes.CYCLE_STATE_PROPERTY;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParams.get(LycheeLootContextParams.BLOCK_POS).offset(offset);
		var level = context.level();
		var oldState = level.getBlockState(pos);
		var state = reversed ? cycleReversed(oldState, property()) : oldState.cycle(property());
		if (!level.setBlockAndUpdate(pos, state)) {
			return;
		}
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
	}

	private static <T extends Comparable<T>> BlockState cycleReversed(BlockState oldState, Property<T> property) {
		T value = oldState.getValue(property);
		Collection<T> values = property.getPossibleValues();
		T last = null;
		for (var v : values) {
			if (last != null && v == value) {
				return oldState.setValue(property, last);
			}
			last = v;
		}
		return last == null ? oldState : oldState.setValue(property, last);
	}

	public Property<?> property() {
		return propertySupplier.get();
	}

	public static class Type implements PostActionType<CycleStateProperty> {
		public static final MapCodec<CycleStateProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(CycleStateProperty::commonProperties),
						BlockPredicateExtensions.CODEC.fieldOf("block").forGetter(CycleStateProperty::block),
						LycheeCodecs.OFFSET.forGetter(CycleStateProperty::offset),
						ExtraCodecs.NON_EMPTY_STRING.fieldOf("property").forGetter(CycleStateProperty::propertyName),
						Codec.BOOL.optionalFieldOf("reversed", false).forGetter(CycleStateProperty::reversed))
				.apply(instance, CycleStateProperty::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, CycleStateProperty> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				CycleStateProperty::commonProperties,
				BlockPredicate.STREAM_CODEC,
				CycleStateProperty::block,
				BlockPos.STREAM_CODEC,
				CycleStateProperty::offset,
				ByteBufCodecs.STRING_UTF8,
				CycleStateProperty::propertyName,
				ByteBufCodecs.BOOL,
				CycleStateProperty::reversed,
				CycleStateProperty::new);

		@Override
		public MapCodec<CycleStateProperty> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CycleStateProperty> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
