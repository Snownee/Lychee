package snownee.lychee.core.post;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.mixin.BlockPredicateAccess;
import snownee.lychee.mixin.NbtPredicateAccess;
import snownee.lychee.mixin.StatePropertiesPredicateAccess;
import snownee.lychee.util.LUtil;

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
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		BlockPos pos = ctx.getParamOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = new BlockPos(ctx.getParam(LootContextParams.ORIGIN));
		}
		pos = pos.offset(offset);
		ServerLevel level = ctx.getServerLevel();
		BlockState oldState = level.getBlockState(pos);
		BlockPredicateAccess access = (BlockPredicateAccess) block;
		Set<String> properties = ((StatePropertiesPredicateAccess) access.getProperties()).getProperties().stream().map($ -> $.getName()).collect(Collectors.toSet());
		BlockState state = BlockPredicateHelper.anyBlockState(block);
		if (state.isAir()) {
			destroyBlock(level, pos, false);
			return;
		}
		if (recipe.getType() == RecipeTypes.BLOCK_CRUSHING && !oldState.isAir()) {
			level.levelEvent(2001, pos, Block.getId(oldState));
		}
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property property = entry.getKey();
			if (properties.contains(property.getName()) || !state.hasProperty(property))
				continue;
			state = state.setValue(property, (Comparable) entry.getValue());
		}
		if (state.hasProperty(BlockStateProperties.WATERLOGGED) && oldState.getFluidState().isSourceOfType(Fluids.WATER)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		if (!level.setBlockAndUpdate(pos, state)) {
			return;
		}

		NbtPredicate nbtPredicate = access.getNbt();
		if (nbtPredicate != NbtPredicate.ANY) {
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
	}

	private static boolean destroyBlock(Level level, BlockPos pos, boolean drop) {
		BlockState blockstate = level.getBlockState(pos);
		if (blockstate.isAir()) {
			return false;
		} else {
			FluidState fluidstate = level.getFluidState(pos);
			if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
				level.levelEvent(2001, pos, Block.getId(blockstate));
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
		String key = LUtil.makeDescriptionId("postAction", getType().getRegistryName());
		if (state.isAir()) {
			return new TranslatableComponent(key + ".consume");
		}
		return new TranslatableComponent(key, state.getBlock().getName());
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return BlockPredicateHelper.getMatchedItemStacks(block);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		BlockState state = BlockPredicateHelper.anyBlockState(block);
		if (state.isAir()) {
			GuiGameElement.of(Items.BARRIER).render(poseStack, x, y);
			return;
		}
		GuiGameElement.of(state).rotateBlock(22.5f, 45f, 0).scale(10).atLocal(0.3, 1.3, 2).render(poseStack, x, y);
	}

	@Override
	public boolean canRepeat() {
		return false;
	}

	public static class Type extends PostActionType<PlaceBlock> {

		@Override
		public PlaceBlock fromJson(JsonObject o) {
			int x = GsonHelper.getAsInt(o, "offsetX", 0);
			int y = GsonHelper.getAsInt(o, "offsetY", 0);
			int z = GsonHelper.getAsInt(o, "offsetZ", 0);
			BlockPos offset = BlockPos.ZERO;
			if (x != 0 || y != 0 || z != 0) {
				offset = new BlockPos(x, y, z);
			}
			return new PlaceBlock(BlockPredicateHelper.fromJson(o.get("block")), offset);
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
