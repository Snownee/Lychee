package snownee.lychee.action;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
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
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public final class PlaceBlock implements PostAction {
	private final PostActionCommonProperties commonProperties;
	private final BlockPredicate block;
	private final BlockPos offset;
	private final boolean fancyDisplay;

	public PlaceBlock(
			PostActionCommonProperties properties,
			BlockPredicate block,
			BlockPos offset) {
		this.block = block;
		this.offset = offset;
		this.commonProperties = properties;
		this.fancyDisplay = properties.icon() == null && BlockPredicateExtensions.isAny(block) && offset.equals(BlockPos.ZERO);
	}

	public boolean fancyDisplay() {
		return fancyDisplay;
	}

	@Override
	public boolean hidden() {
		return fancyDisplay() || PostAction.super.hidden();
	}

	private static void destroyBlock(Level level, BlockPos pos, boolean drop) {
		var blockstate = level.getBlockState(pos);
		if (blockstate.isAir()) {
			return;
		}
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
		var pos = lootParamsContext.getOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = BlockPos.containing(lootParamsContext.get(LootContextParams.ORIGIN));
		}
		pos = pos.offset(offset);
		var level = context.level();
		var oldState = level.getBlockState(pos);
		var blockState = BlockPredicateExtensions.anyBlockState(block);
		if (blockState.isAir()) {
			destroyBlock(level, pos, false);
			return;
		}
		if (recipe instanceof BlockCrushingRecipe && !oldState.isAir()) {
			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oldState));
		}

		var properties = block.properties()
				.map(StatePropertiesPredicate::properties)
				.stream()
				.flatMap(Collection::stream)
				.map(StatePropertiesPredicate.PropertyMatcher::name)
				.collect(Collectors.toSet());
		for (var entry : oldState.getValues().entrySet()) {
			var property = entry.getKey();
			if (!properties.contains(property.getName())) {
				//noinspection rawtypes,unchecked
				blockState = blockState.trySetValue((Property) property, (Comparable) entry.getValue());
			}
		}
		if (oldState.getFluidState().isSourceOfType(Fluids.WATER)) {
			blockState = blockState.trySetValue(BlockStateProperties.WATERLOGGED, true);
		}

		if (!level.setBlockAndUpdate(pos, blockState)) {
			return;
		}

		setNbt:
		if (block.nbt().isPresent()) {
			var blockEntity = level.getBlockEntity(pos);
			if (blockEntity == null || blockEntity.onlyOpCanSetNbt()) {
				break setNbt;
			}

			var prevTag = blockEntity.saveWithoutMetadata(level.registryAccess());
			var originalTag = prevTag.copy();
			prevTag.merge(block.nbt().get().tag());
			if (prevTag.equals(originalTag)) {
				break setNbt;
			}

			blockEntity.loadWithComponents(prevTag, level.registryAccess());
			blockEntity.setChanged();
		}
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(blockState));
	}

	@Override
	public Component getDisplayName() {
		var state = BlockPredicateExtensions.anyBlockState(block);
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
		return BlockPredicateExtensions.isAny(block) ? List.of() : List.of(block);
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	public BlockPredicate block() {
		return block;
	}

	public BlockPos offset() {
		return offset;
	}

	public static class Type implements PostActionType<PlaceBlock> {
		public static final MapCodec<PlaceBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(PlaceBlock::commonProperties),
				BlockPredicateExtensions.CODEC.optionalFieldOf("block", BlockPredicateExtensions.ANY).forGetter(PlaceBlock::block),
				LycheeCodecs.OFFSET_CODEC.forGetter(PlaceBlock::offset)
		).apply(instance, PlaceBlock::new));

		@Override
		public MapCodec<PlaceBlock> codec() {
			return CODEC;
		}
	}
}
