package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
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

public record MoveTowardsFace(PostActionCommonProperties commonProperties, float factor) implements PostAction {

	@Override
	public PostActionType<MoveTowardsFace> type() {
		return PostActionTypes.MOVE_TOWARDS_FACE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParams.get(LootContextParams.ORIGIN);
		var vector = new Vec3(lootParams.get(LycheeLootContextParams.DIRECTION).step()).scale(factor);
		lootParams.set(LootContextParams.ORIGIN, pos.add(vector));
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	public static class Type implements PostActionType<MoveTowardsFace> {
		public static final MapCodec<MoveTowardsFace> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(MoveTowardsFace::commonProperties),
				Codec.FLOAT.optionalFieldOf("factor", 1F).forGetter(MoveTowardsFace::factor)
		).apply(inst, MoveTowardsFace::new));

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, MoveTowardsFace> streamCodec() {
			throw new UnsupportedOperationException();
		}

		@Override
		public MapCodec<MoveTowardsFace> codec() {
			return CODEC;
		}
	}
}
