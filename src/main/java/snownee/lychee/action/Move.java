package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
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

public record Move(PostActionCommonProperties commonProperties, Vec3 offset) implements PostAction {

	@Override
	public PostActionType<Move> type() {
		return PostActionTypes.MOVE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParamsContext.getOrNull(LootContextParams.ORIGIN);
		if (pos == null) {
			pos = Vec3.atCenterOf(lootParamsContext.get(LycheeLootContextParams.BLOCK_POS));
		}
		lootParamsContext.setParam(LootContextParams.ORIGIN, pos.add(offset));
		lootParamsContext.removeParam(LycheeLootContextParams.BLOCK_POS);
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	public static class Type implements PostActionType<Move> {
		public static final MapCodec<Move> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Move::commonProperties),
				Vec3.CODEC.fieldOf("offset").forGetter(Move::offset)
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
