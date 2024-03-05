package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public final class CycleStateProperty extends PlaceBlock {
	public final String property;
	private final Supplier<Property<?>> propertySupplier;

	public CycleStateProperty(PostActionCommonProperties commonProperties, BlockPredicate block, BlockPos offset, String property) {
		super(commonProperties, block, offset);
		this.property = property;
		this.propertySupplier = Suppliers.memoize(() -> findProperty(block, property));
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
	protected @Nullable BlockState getNewState(BlockState oldState) {
		try {
			return oldState.cycle(propertySupplier.get());
		} catch (Throwable e) {
			return null;
		}
	}

	public Property<?> property() {return propertySupplier.get();}

	public static class Type implements PostActionType<CycleStateProperty> {
		public static final Codec<CycleStateProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(CycleStateProperty::commonProperties),
				BlockPredicate.CODEC.fieldOf("block").forGetter(it -> it.block),
				RecordCodecBuilder.<BlockPos>mapCodec(posInstance -> posInstance.group(
						Codec.INT.fieldOf("offsetX").forGetter(Vec3i::getX),
						Codec.INT.fieldOf("offsetY").forGetter(Vec3i::getY),
						Codec.INT.fieldOf("offsetZ").forGetter(Vec3i::getZ)
				).apply(posInstance, BlockPos::new)).forGetter(it -> it.offset),
				Codec.STRING.fieldOf("property").forGetter(it -> it.property)
		).apply(instance, CycleStateProperty::new));

		@Override
		public Codec<CycleStateProperty> codec() {
			return null;
		}
	}

}
