package snownee.lychee.core.recipe.recipe;

import java.util.function.Function;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicate;
import snownee.lychee.util.recipe.BlockKeyableRecipe;

public abstract class ItemAndBlockRecipe<C extends LycheeRecipeContext> extends OldLycheeRecipe<C>
		implements BlockKeyableRecipe<ItemAndBlockRecipe<C>> {

	protected Ingredient input;
	protected BlockPredicate block;

	public ItemAndBlockRecipe(ResourceLocation id) {
		super(id);
	}

	public Ingredient getInput() {
		return input;
	}

	@Override
	public BlockPredicate blockPredicate() {
		return block;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
	}

	@Override
	public boolean matches(LycheeRecipeContext ctx, Level pLevel) {
		ItemStack stack;
		Entity thisEntity = ctx.getParam(LootContextParams.THIS_ENTITY);
		if (thisEntity instanceof ItemEntity) {
			stack = ((ItemEntity) thisEntity).getItem();
		} else {
			stack = ctx.getItem(0);
		}
		return input.test(stack) && BlockPredicateHelper.matches(block, ctx);
	}

	@Override
	public int compareTo(ItemAndBlockRecipe<C> that) {
		int i;
		i = Integer.compare(getMaxRepeats().isAny() ? 1 : 0, that.getMaxRepeats().isAny() ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(block == BlockPredicate.ANY ? 1 : 0, that.block == BlockPredicate.ANY ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(
				CommonProxy.isSimpleIngredient(input) ? 1 : 0,
				CommonProxy.isSimpleIngredient(that.input) ? 1 : 0
		);
		if (i != 0)
			return i;
		return getId().compareTo(that.getId());
	}

	public static class Serializer<T extends ItemAndBlockRecipe<?>> extends OldLycheeRecipe.Serializer<T> {

		public Serializer(Function<ResourceLocation, T> factory) {
			super(factory);
		}

		@Override
		public void fromJson(T pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.input = parseIngredientOrAir(pSerializedRecipe.get("item_in"));
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
		}

		@Override
		public void fromNetwork(T pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.input = Ingredient.fromNetwork(pBuffer);
			pRecipe.block = BlockPredicateHelper.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, T pRecipe) {
			pRecipe.input.toNetwork(pBuffer);
			BlockPredicateHelper.toNetwork(pRecipe.block, pBuffer);
		}

	}

}
