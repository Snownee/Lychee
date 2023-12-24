package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostActionType;

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
			BlockPos offset = CommonProxy.parseOffset(o);
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
