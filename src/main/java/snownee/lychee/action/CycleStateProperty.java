package snownee.lychee.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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

public final class CycleStateProperty implements PostAction {
	public final String property;
	private final Supplier<Property<?>> propertySupplier;
	private final PostActionCommonProperties commonProperties;
	private final BlockPredicate block;
	private final BlockPos offset;

	public CycleStateProperty(
			PostActionCommonProperties commonProperties,
			BlockPredicate block,
			BlockPos offset,
			String property
	) {
		this.property = property;
		this.commonProperties = commonProperties;
		this.block = block;
		this.offset = offset;
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
	public PostActionCommonProperties commonProperties() {
		return commonProperties;
	}

	@Override
	public PostActionType<CycleStateProperty> type() {
		return PostActionTypes.CYCLE_STATE_PROPERTY;
	}

	public BlockPredicate block() {
		return block;
	}

	public BlockPos offset() {
		return offset;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var blockPos = lootParamsContext.getOrNull(LycheeLootContextParams.BLOCK_POS);
		if (blockPos == null) {
			blockPos = BlockPos.containing(lootParamsContext.get(LootContextParams.ORIGIN));
		}
		blockPos = blockPos.offset(offset);
		var level = context.get(LycheeContextKey.LEVEL);
		var oldState = level.getBlockState(blockPos);
		var state = oldState.cycle(propertySupplier.get());
		if (!level.setBlockAndUpdate(blockPos, state)) {
			return;
		}
		level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(state));
	}

	public Property<?> property() {return propertySupplier.get();}

	public static class Type implements PostActionType<CycleStateProperty> {
		public static final Codec<CycleStateProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(CycleStateProperty::commonProperties),
				BlockPredicateExtensions.CODEC.fieldOf("block").forGetter(it -> it.block),
				LycheeCodecs.OFFSET_CODEC.forGetter(it -> it.offset),
				Codec.STRING.fieldOf("property").forGetter(it -> it.property)
		).apply(instance, CycleStateProperty::new));
		@Override
		public @NotNull Codec<CycleStateProperty> codec() {
			return CODEC;
		}
	}
}
