package snownee.lychee.contextual;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public final class IsSneaking implements ContextualCondition {
	public static final IsSneaking INSTANCE = new IsSneaking();

	@Override
	public ContextualConditionType<IsSneaking> type() {
		return ContextualConditionType.IS_SNEAKING;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.get(LycheeContextKey.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		return entity.isCrouching() || entity.isShiftKeyDown() ? times : 0;
	}

	public static class Type implements ContextualConditionType<IsSneaking> {
		public static final MapCodec<IsSneaking> CODEC = MapCodec.unit(INSTANCE);

		@Override
		public MapCodec<IsSneaking> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, IsSneaking> streamCodec() {
			return StreamCodec.unit(INSTANCE);
		}
	}
}
