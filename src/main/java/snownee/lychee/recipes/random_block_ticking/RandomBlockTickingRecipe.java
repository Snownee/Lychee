package snownee.lychee.recipes.random_block_ticking;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.recipe.ChanceRecipe;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class RandomBlockTickingRecipe extends OldLycheeRecipe<LycheeRecipeContext>
    implements BlockKeyableRecipe<RandomBlockTickingRecipe>, ChanceRecipe {

	protected float chance = 1;
	protected BlockPredicate block;

	public RandomBlockTickingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public int compareTo(RandomBlockTickingRecipe that) {
		int i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0)
			return i;
		return getId().compareTo(that.getId());
	}

	@Override
	public boolean matches(LycheeRecipeContext ctx, Level level) {
		return BlockPredicateHelper.matches(block, ctx);
	}

	@Override
	public BlockPredicate blockPredicate() {
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
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.RANDOM_BLOCK_TICKING;
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.RANDOM_BLOCK_TICKING;
	}

	public static class Serializer extends OldLycheeRecipe.Serializer<RandomBlockTickingRecipe> {

		public Serializer() {
			super(RandomBlockTickingRecipe::new);
		}

		@Override
		public void fromJson(RandomBlockTickingRecipe pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
			Preconditions.checkArgument(
					pRecipe.block != BlockPredicate.ANY,
					"Wildcard block input is not allowed for this recipe type."
			);
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
