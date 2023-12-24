package snownee.lychee.contextual;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.recipe.OldLycheeRecipe;

public final class IsSneaking implements ContextualCondition<IsSneaking> {
	private static final IsSneaking INSTANCE = new IsSneaking();

	@Override
	public ContextualConditionType<IsSneaking> type() {
		return ContextualConditionTypes.IS_SNEAKING;
	}

	@Override
	public int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		Entity entity = ctx.get(LootContextParams.THIS_ENTITY);
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
