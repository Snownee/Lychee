package snownee.lychee.core.post;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.PostActionTypes;
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

	public PlaceBlock(BlockPredicate block) {
		this.block = block;
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
		ServerLevel level = ctx.getServerLevel();
		BlockState oldState = level.getBlockState(pos);
		BlockPredicateAccess access = (BlockPredicateAccess) block;
		Set<String> properties = ((StatePropertiesPredicateAccess) access.getProperties()).getProperties().stream().map($ -> $.getName()).collect(Collectors.toSet());
		BlockState state = BlockPredicateHelper.anyBlockState(block);
		if (state.isAir()) {
			level.destroyBlock(pos, false);
			return;
		}
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property property = entry.getKey();
			if (properties.contains(property.getName()) || !state.hasProperty(property))
				continue;
			state = state.setValue(property, (Comparable) entry.getValue());
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
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		BlockState state = BlockPredicateHelper.anyBlockState(block);
		if (state.isAir()) {
			GuiGameElement.of(Items.BARRIER).render(poseStack, x, y);
			return;
		}
		GuiGameElement.of(state).rotateBlock(22.5f, 45f, 0).scale(10).atLocal(0.3, 1.3, 2).render(poseStack, x, y);
	}

	public static class Type extends PostActionType<PlaceBlock> {

		@Override
		public PlaceBlock fromJson(JsonObject o) {
			return new PlaceBlock(BlockPredicateHelper.fromJson(o.get("block")));
		}

		@Override
		public PlaceBlock fromNetwork(FriendlyByteBuf buf) {
			return new PlaceBlock(BlockPredicateHelper.fromNetwork(buf));
		}

		@Override
		public void toNetwork(PlaceBlock condition, FriendlyByteBuf buf) {
			BlockPredicateHelper.toNetwork(condition.block, buf);
		}

		@Override
		public boolean canBatchRun() {
			return false;
		}

	}

}
