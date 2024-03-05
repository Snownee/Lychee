package snownee.lychee.action;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class PlaceBlock implements PostAction {
	private final PostActionCommonProperties commonProperties;

	public final BlockPredicate block;
	public final BlockPos offset;

	public PlaceBlock(PostActionCommonProperties commonProperties, BlockPredicate block, BlockPos offset) {
		this.commonProperties = commonProperties;
		this.block = block;
		this.offset = offset;
	}

	private static boolean destroyBlock(Level level, BlockPos pos, boolean drop) {
		var blockstate = level.getBlockState(pos);
		if (blockstate.isAir()) {
			return false;
		} else {
			var fluidstate = level.getFluidState(pos);
			if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockstate));
			}

			if (drop) {
				var blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(pos) : null;
				Block.dropResources(blockstate, level, pos, blockentity, null, ItemStack.EMPTY);
			}

			var legacy = fluidstate.createLegacyBlock();
			if (legacy == blockstate) {
				legacy = Blocks.AIR.defaultBlockState();
			}
			var flag = level.setBlock(pos, legacy, 3, 512);
			if (flag) {
				level.gameEvent(null, GameEvent.BLOCK_DESTROY, pos);
			}

			return flag;
		}
	}

	@Override
	public PostActionCommonProperties commonProperties() {
		return commonProperties;
	}

	@Override
	public PostActionType<? extends PlaceBlock> type() {
		return PostActionTypes.PLACE;
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
		var state = getNewState(oldState);
		if (state == null) {
			return;
		}
		if (state.isAir()) {
			destroyBlock(level, blockPos, false);
			return;
		}
		if (recipe instanceof BlockCrushingRecipe && !oldState.isAir()) {
			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, blockPos, Block.getId(oldState));
		}

		if (type() == PostActionTypes.PLACE) {
			var properties = block.properties()
					.map(StatePropertiesPredicate::properties)
					.stream()
					.flatMap(Collection::stream)
					.map(StatePropertiesPredicate.PropertyMatcher::name)
					.collect(Collectors.toSet());
			for (var entry : oldState.getValues().entrySet()) {
				var property = entry.getKey();
				if (properties.contains(property.getName()) || !state.hasProperty(property)) {
					continue;
				}
				state = state.setValue(property, (Comparable) entry.getValue());
			}
			if (state.hasProperty(BlockStateProperties.WATERLOGGED) && oldState.getFluidState().isSourceOfType(Fluids.WATER)) {
				state = state.setValue(BlockStateProperties.WATERLOGGED, true);
			}
		}

		if (!level.setBlockAndUpdate(blockPos, state)) {
			return;
		}

		if (type() == PostActionTypes.PLACE && block.nbt().isPresent()) {
			var blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity != null) {
				if (blockEntity.onlyOpCanSetNbt()) {
					return;
				}

				var prevTag = blockEntity.saveWithoutMetadata(level.registryAccess());
				var originalTag = prevTag.copy();
				prevTag.merge(block.nbt().get().tag());
				if (!prevTag.equals(originalTag)) {
					blockEntity.load(prevTag, level.registryAccess());
					blockEntity.setChanged();
				}
			}
		}
		level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(state));
	}

	@Nullable
	protected BlockState getNewState(BlockState oldState) {
		return BlockPredicateExtensions.anyBlockState(block);
	}

	@Override
	public Component getDisplayName() {
		BlockState state = BlockPredicateExtensions.anyBlockState(block);
		var key = CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(PostActionTypes.PLACE));
		if (state.isAir()) {
			return Component.translatable(key + ".consume");
		}
		return Component.translatable(key, state.getBlock().getName());
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return BlockPredicateExtensions.matchedItemStacks(block);
	}

	@Override
	public List<BlockPredicate> getOutputBlocks() {
		return List.of(block);
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PlaceBlock that = (PlaceBlock) o;
		return Objects.equal(commonProperties, that.commonProperties) &&
				Objects.equal(block, that.block) && Objects.equal(
				offset,
				that.offset
		);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(commonProperties, block, offset);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("commonProperties", commonProperties)
				.add("block", block)
				.add("offset", offset)
				.toString();
	}

	public static class Type implements PostActionType<PlaceBlock> {
		public static final Codec<PlaceBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(PlaceBlock::commonProperties),
				BlockPredicate.CODEC.fieldOf("block").forGetter(it -> it.block),
				RecordCodecBuilder.<BlockPos>mapCodec(posInstance -> posInstance.group(
						Codec.INT.fieldOf("offsetX").forGetter(Vec3i::getX),
						Codec.INT.fieldOf("offsetY").forGetter(Vec3i::getY),
						Codec.INT.fieldOf("offsetZ").forGetter(Vec3i::getZ)
				).apply(posInstance, BlockPos::new)).forGetter(it -> it.offset)
		).apply(instance, PlaceBlock::new));

		@Override
		public Codec<PlaceBlock> codec() {
			return CODEC;
		}
	}
}
