package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Move(PostActionCommonProperties commonProperties, Vec3 offset, String with) implements PostAction {

	@Override
	public PostActionType<Move> type() {
		return PostActionTypes.MOVE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		Vec3 offset = this.offset;
		if (!with.isEmpty()) {
			BlockState blockState = lootParams.getOrNull(LootContextParams.BLOCK_STATE);
			if (blockState == null) {
				return;
			}
			var property = blockState.getBlock().getStateDefinition().getProperty(with);
			if (!(property instanceof DirectionProperty directionProperty)) {
				return;
			}
			Direction direction = blockState.getValue(directionProperty);
			offset = switch (direction) {
				case DOWN -> offset.xRot(Mth.PI);
				case UP -> offset;
				case NORTH -> offset.xRot(Mth.HALF_PI);
				case SOUTH -> offset.xRot(Mth.HALF_PI).yRot(Mth.PI);
				case WEST -> offset.xRot(Mth.HALF_PI).yRot(Mth.HALF_PI);
				case EAST -> offset.xRot(Mth.HALF_PI).yRot(-Mth.HALF_PI);
			};
		}
		var pos = lootParams.get(LootContextParams.ORIGIN);
		lootParams.set(LootContextParams.ORIGIN, pos.add(offset));
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	public static class Type implements PostActionType<Move> {
		public static final MapCodec<Move> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Move::commonProperties),
				Vec3.CODEC.fieldOf("offset").forGetter(Move::offset),
				Codec.STRING.optionalFieldOf("with", "").forGetter(Move::with)
		).apply(inst, Move::new));

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Move> streamCodec() {
			throw new UnsupportedOperationException();
		}

		@Override
		public MapCodec<Move> codec() {
			return CODEC;
		}
	}
}
