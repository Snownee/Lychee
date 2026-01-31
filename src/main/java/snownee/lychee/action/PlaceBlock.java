package snownee.lychee.action;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import snownee.lychee.LootContextKeys;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Displays;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record PlaceBlock(
		PostActionCommonProperties commonProperties,
		BlockPredicate block,
		BlockPos offset,
		boolean fancyDisplay) implements PostAction {

	public PlaceBlock(PostActionCommonProperties commonProperties, BlockPredicate block, BlockPos offset) {
		this(
				commonProperties,
				block,
				offset,
				commonProperties.icon() == null && BlockPredicateExtensions.isAny(block) && offset.equals(BlockPos.ZERO));
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
	public PostActionType<? extends PlaceBlock> type() {
		return PostActionTypes.PLACE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParams.get(LootContextKeys.BLOCK_POS).offset(offset);
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
			if (blockEntity == null || blockEntity.getType().onlyOpCanSetNbt()) {
				break setNbt;
			}

			HolderLookup.Provider registries = level.registryAccess();
			var prevTag = blockEntity.saveWithoutMetadata(registries);
			var originalTag = prevTag.copy();
			prevTag.merge(block.nbt().get().tag());
			if (prevTag.equals(originalTag)) {
				break setNbt;
			}

			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Lychee.LOGGER)) {
				blockEntity.loadWithComponents(TagValueInput.create(reporter, registries, prevTag));
				blockEntity.setChanged();
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.getChunkSource().blockChanged(pos);
				}
			}
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
	public List<SlotDisplay> getOutputItems() {
		return BlockPredicateExtensions.matchedItemStacks(block).stream().map(Displays::slot).toList();
	}

	@Override
	public List<BlockPredicate> getOutputBlocks() {
		return BlockPredicateExtensions.isAny(block) ? List.of() : List.of(block);
	}

	@Override
	public boolean hidden() {
		return fancyDisplay() || PostAction.super.hidden();
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	public static class Type implements PostActionType<PlaceBlock> {
		public static final MapCodec<PlaceBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(PlaceBlock::commonProperties),
				BlockPredicateExtensions.CODEC.fieldOf("block").forGetter(PlaceBlock::block),
				LycheeCodecs.OFFSET.forGetter(PlaceBlock::offset)).apply(instance, PlaceBlock::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, PlaceBlock> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				PlaceBlock::commonProperties,
				BlockPredicate.STREAM_CODEC,
				PlaceBlock::block,
				BlockPos.STREAM_CODEC,
				PlaceBlock::offset,
				PlaceBlock::new);

		@Override
		public MapCodec<PlaceBlock> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, PlaceBlock> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
