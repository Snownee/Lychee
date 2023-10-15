package snownee.lychee.core.post;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.mixin.BlockPredicateAccess;
import snownee.lychee.mixin.NbtPredicateAccess;
import snownee.lychee.mixin.StatePropertiesPredicateAccess;
import snownee.lychee.util.CommonProxy;

public class PlaceBlock extends PostAction {

	public final BlockPredicate block;
	public final BlockPos offset;

	public PlaceBlock(BlockPredicate block, BlockPos offset) {
		this.block = block;
		this.offset = offset;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.PLACE;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		BlockPos pos = ctx.getParamOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = BlockPos.containing(ctx.getParam(LootContextParams.ORIGIN));
		}
		pos = pos.offset(offset);
		ServerLevel level = ctx.getServerLevel();
		BlockState oldState = level.getBlockState(pos);
		BlockState state = getNewState(oldState);
		if (state == null) {
			return;
		}
		if (state.isAir()) {
			destroyBlock(level, pos, false);
			return;
		}
		if (recipe instanceof BlockCrushingRecipe && !oldState.isAir()) {
			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oldState));
		}
		BlockPredicateAccess access = (BlockPredicateAccess) block;
		if (getType() == PostActionTypes.PLACE) {
			Set<String> properties = ((StatePropertiesPredicateAccess) access.getProperties()).getProperties().stream().map($ -> $.getName()).collect(Collectors.toSet());
			for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
				Property property = entry.getKey();
				if (properties.contains(property.getName()) || !state.hasProperty(property))
					continue;
				state = state.setValue(property, (Comparable) entry.getValue());
			}
			if (state.hasProperty(BlockStateProperties.WATERLOGGED) && oldState.getFluidState().isSourceOfType(Fluids.WATER)) {
				state = state.setValue(BlockStateProperties.WATERLOGGED, true);
			}
		}
		if (!level.setBlockAndUpdate(pos, state)) {
			return;
		}

		NbtPredicate nbtPredicate = access.getNbt();
		if (getType() == PostActionTypes.PLACE && nbtPredicate != NbtPredicate.ANY) {
			BlockEntity blockentity = level.getBlockEntity(pos);
			if (blockentity != null) {
				if (blockentity.onlyOpCanSetNbt()) {
					return;
				}

				CompoundTag compoundtag1 = blockentity.saveWithoutMetadata();
				CompoundTag compoundtag2 = compoundtag1.copy();
				compoundtag1.merge(((NbtPredicateAccess) nbtPredicate).getTag());
				if (!compoundtag1.equals(compoundtag2)) {
					blockentity.load(compoundtag1);
					blockentity.setChanged();
				}
			}
		}
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
	}

	@Nullable
	protected BlockState getNewState(BlockState oldState) {
		return BlockPredicateHelper.anyBlockState(block);
	}

	private static boolean destroyBlock(Level level, BlockPos pos, boolean drop) {
		BlockState blockstate = level.getBlockState(pos);
		if (blockstate.isAir()) {
			return false;
		} else {
			FluidState fluidstate = level.getFluidState(pos);
			if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockstate));
			}

			if (drop) {
				BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(pos) : null;
				Block.dropResources(blockstate, level, pos, blockentity, null, ItemStack.EMPTY);
			}

			BlockState legacy = fluidstate.createLegacyBlock();
			if (legacy == blockstate) {
				legacy = Blocks.AIR.defaultBlockState();
			}
			boolean flag = level.setBlock(pos, legacy, 3, 512);
			if (flag) {
				level.gameEvent(null, GameEvent.BLOCK_DESTROY, pos);
			}

			return flag;
		}
	}

	@Override
	public Component getDisplayName() {
		BlockState state = BlockPredicateHelper.anyBlockState(block);
		String key = CommonProxy.makeDescriptionId("postAction", PostActionTypes.PLACE.getRegistryName());
		if (state.isAir()) {
			return Component.translatable(key + ".consume");
		}
		return Component.translatable(key, state.getBlock().getName());
	}

	@Override
	public List<ItemStack> getItemOutputs() {
		return BlockPredicateHelper.getMatchedItemStacks(block);
	}

	@Override
	public List<BlockPredicate> getBlockOutputs() {
		return List.of(block);
	}

	@Override
	public boolean canRepeat() {
		return false;
	}

	public static class Type extends PostActionType<PlaceBlock> {

		@Override
		public PlaceBlock fromJson(JsonObject o) {
			return new PlaceBlock(BlockPredicateHelper.fromJson(o.get("block")), CommonProxy.parseOffset(o));
		}

		@Override
		public void toJson(PlaceBlock action, JsonObject o) {
			BlockPos offset = action.offset;
			if (offset.getX() != 0) {
				o.addProperty("offsetX", offset.getX());
			}
			if (offset.getY() != 0) {
				o.addProperty("offsetY", offset.getY());
			}
			if (offset.getZ() != 0) {
				o.addProperty("offsetZ", offset.getX());
			}
			o.add("block", BlockPredicateHelper.toJson(action.block));
		}

		@Override
		public PlaceBlock fromNetwork(FriendlyByteBuf buf) {
			return new PlaceBlock(BlockPredicateHelper.fromNetwork(buf), buf.readBlockPos());
		}

		@Override
		public void toNetwork(PlaceBlock action, FriendlyByteBuf buf) {
			BlockPredicateHelper.toNetwork(action.block, buf);
			buf.writeBlockPos(action.offset);
		}

	}

}
