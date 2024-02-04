package snownee.lychee.contextual;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.LycheeRecipe;

public final class IsSneaking implements ContextualCondition<IsSneaking> {
	private static final IsSneaking INSTANCE = new IsSneaking();

	@Override
	public ContextualConditionType<IsSneaking> type() {
		return ContextualConditionType.IS_SNEAKING;
	}

	@Override
	public int test(RecipeHolder<LycheeRecipe<?>> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.get(LycheeContextType.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		return entity.isCrouching() || entity.isShiftKeyDown() ? times : 0;
	}

	public static class Type implements ContextualConditionType<IsSneaking> {
		public static final Codec<IsSneaking> CODEC = Codec.unit(INSTANCE);

		@Override
		public Codec<IsSneaking> codec() {
			return CODEC;
		}

		@Override
		public IsSneaking fromNetwork(FriendlyByteBuf buf) {
			return IsSneaking.INSTANCE;
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, IsSneaking condition) {
		}
	}
}