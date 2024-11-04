package snownee.lychee.random_block_ticking;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ChanceRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class RandomBlockTickingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyRecipe<RandomBlockTickingRecipe>, ChanceRecipe {

	protected float chance = 1;
	protected BlockPredicate block;

	public RandomBlockTickingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public int compareTo(RandomBlockTickingRecipe that) {
		int i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		return getId().compareTo(that.getId());
	}

	@Override
	public boolean matches(LycheeContext ctx, Level level) {
		return BlockPredicateHelper.fastMatch(block, ctx);
	}

	@Override
	public BlockPredicate getBlock() {
		return block;
	}

	@Override
	public float getChance() {
		return chance;
	}

	@Override
	public void setChance(float chance) {
		this.chance = chance;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.RANDOM_BLOCK_TICKING;
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return RecipeSerializers.RANDOM_BLOCK_TICKING;
	}

	public static class Serializer extends LycheeRecipe.Serializer<RandomBlockTickingRecipe> {

		public Serializer() {
			super(RandomBlockTickingRecipe::new);
		}

		@Override
		public void fromJson(RandomBlockTickingRecipe pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
			if (!pRecipe.ghost) {
				Preconditions.checkArgument(pRecipe.block != BlockPredicate.ANY,
						"Wildcard block input is not allowed for this recipe type.");
			}
		}

		@Override
		public void fromNetwork(RandomBlockTickingRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.block = BlockPredicateHelper.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, RandomBlockTickingRecipe pRecipe) {
			BlockPredicateHelper.toNetwork(pRecipe.block, pBuffer);
		}

	}

}
