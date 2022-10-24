package snownee.lychee.core.post;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.util.LUtil;

public class CycleStateProperty extends PlaceBlock {

	public final Property<?> property;

	public CycleStateProperty(BlockPredicate block, BlockPos offset, Property<?> property) {
		super(block, offset);
		this.property = property;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.CYCLE_STATE_PROPERTY;
	}

	@Override
	protected @Nullable BlockState getNewState(BlockState oldState) {
		try {
			return oldState.cycle(property);
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		List<BlockState> states = BlockPredicateHelper.getShowcaseBlockStates(block, Set.of(property));
		BlockState state = LUtil.getCycledItem(states, Blocks.AIR.defaultBlockState(), 1000);
		GuiGameElement.of(state).rotateBlock(22.5f, 45f, 0).scale(10).atLocal(0.3, 1.3, 2).render(poseStack, x, y);
	}

	public static Property<?> findProperty(BlockPredicate blockPredicate, String name) {
		BlockState block = BlockPredicateHelper.anyBlockState(blockPredicate);
		for (var property : block.getProperties()) {
			if (name.equals(property.getName())) {
				return property;
			}
		}
		throw new IllegalArgumentException("Unknown property name: " + name);
	}

	public static class Type extends PostActionType<CycleStateProperty> {

		@Override
		public CycleStateProperty fromJson(JsonObject o) {
			int x = GsonHelper.getAsInt(o, "offsetX", 0);
			int y = GsonHelper.getAsInt(o, "offsetY", 0);
			int z = GsonHelper.getAsInt(o, "offsetZ", 0);
			BlockPos offset = BlockPos.ZERO;
			if (x != 0 || y != 0 || z != 0) {
				offset = new BlockPos(x, y, z);
			}
			BlockPredicate block = BlockPredicateHelper.fromJson(o.get("block"));
			return new CycleStateProperty(block, offset, findProperty(block, GsonHelper.getAsString(o, "property")));
		}

		@Override
		public void toJson(CycleStateProperty action, JsonObject o) {
			PostActionTypes.PLACE.toJson(action, o);
			o.addProperty("property", action.property.getName());
		}

		@Override
		public CycleStateProperty fromNetwork(FriendlyByteBuf buf) {
			BlockPredicate block = BlockPredicateHelper.fromNetwork(buf);
			return new CycleStateProperty(block, buf.readBlockPos(), findProperty(block, buf.readUtf()));
		}

		@Override
		public void toNetwork(CycleStateProperty action, FriendlyByteBuf buf) {
			PostActionTypes.PLACE.toNetwork(action, buf);
			buf.writeUtf(action.property.getName());
		}

	}

}
