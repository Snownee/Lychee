package snownee.lychee.recipes.block_exploding;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class BlockExplodingRecipe extends OldLycheeRecipe<BlockExplodingContext>
    implements BlockKeyableRecipe<BlockExplodingRecipe> {

	private BlockPredicate block;

	public BlockExplodingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(BlockExplodingContext ctx, Level pLevel) {
		return BlockPredicateHelper.matches(block, ctx);
	}

	@Override
	public BlockPredicate blockPredicate() {
		return block;
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.BLOCK_EXPLODING;
	}

	@Override
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.BLOCK_EXPLODING;
	}

	@Override
	public int compareTo(BlockExplodingRecipe that) {
		int i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(block == BlockPredicate.ANY ? 1 : 0, that.block == BlockPredicate.ANY ? 1 : 0);
		if (i != 0)
			return i;
		return getId().compareTo(that.getId());
	}

	public static class Serializer extends OldLycheeRecipe.Serializer<BlockExplodingRecipe> {

		public Serializer() {
			super(BlockExplodingRecipe::new);
		}

		@Override
		public void fromJson(BlockExplodingRecipe pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
		}

		@Override
		public void fromNetwork(BlockExplodingRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.block = BlockPredicateHelper.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, BlockExplodingRecipe pRecipe) {
			BlockPredicateHelper.toNetwork(pRecipe.block, pBuffer);
		}

	}
}
