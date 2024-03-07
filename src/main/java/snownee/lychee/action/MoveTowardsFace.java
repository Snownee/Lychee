package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record MoveTowardsFace(PostActionCommonProperties commonProperties, float factor) implements PostAction {

	@Override
	public PostActionType<MoveTowardsFace> type() {
		return PostActionTypes.MOVE_TOWARDS_FACE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var blockPos = lootParamsContext.getOrNull(LycheeLootContextParams.BLOCK_POS);
		if (blockPos == null) {
			blockPos = BlockPos.containing(lootParamsContext.get(LootContextParams.ORIGIN));
		}
		var vector = new Vec3(lootParamsContext.get(LycheeLootContextParams.DIRECTION).step()).scale(factor);
		lootParamsContext.setParam(LootContextParams.ORIGIN, vector.add(Vec3.atCenterOf(blockPos)));
		lootParamsContext.removeParam(LycheeLootContextParams.BLOCK_POS);
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	public static class Type implements PostActionType<MoveTowardsFace> {
		public static final Codec<MoveTowardsFace> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(MoveTowardsFace::commonProperties),
				Codec.FLOAT.optionalFieldOf("factor", 1F).forGetter(MoveTowardsFace::factor)
		).apply(inst, MoveTowardsFace::new));

		@Override
		public StreamCodec<? extends ByteBuf, MoveTowardsFace> streamCodec() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Codec<MoveTowardsFace> codec() {
			return CODEC;
		}
	}
}
