package snownee.lychee.core.recipe;

import java.util.function.Function;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;

public abstract class ItemAndBlockRecipe<C extends LycheeContext> extends LycheeRecipe<C> implements BlockKeyRecipe<ItemAndBlockRecipe<C>> {

	protected Ingredient input;
	protected BlockPredicate block;

	public ItemAndBlockRecipe(ResourceLocation id) {
		super(id);
	}

	public Ingredient getInput() {
		return input;
	}

	@Override
	public BlockPredicate getBlock() {
		return block;
	}

	@Override
	public boolean matches(LycheeContext ctx, Level pLevel) {
		ItemStack stack;
		Entity thisEntity = ctx.getParam(LootContextParams.THIS_ENTITY);
		if (thisEntity instanceof ItemEntity) {
			stack = ((ItemEntity) thisEntity).getItem();
		} else {
			stack = ctx.getParam(LootContextParams.TOOL);
		}
		return input.test(stack) && BlockPredicateHelper.fastMatch(block, ctx);
	}

	@Override
	public int compareTo(ItemAndBlockRecipe<C> that) {
		int i;
		i = Integer.compare(isRepeatable() ? 0 : 1, that.isRepeatable() ? 0 : 1);
		if (i != 0)
			return i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(block == BlockPredicate.ANY ? 1 : 0, that.block == BlockPredicate.ANY ? 1 : 0);
		if (i != 0)
			return i;
		//		i = Integer.compare(input.isSimple() ? 1 : 0, that.input.isSimple() ? 1 : 0);
		//		if (i != 0)
		//			return i;
		return getId().compareTo(that.getId());
	}

	public static class Serializer<T extends ItemAndBlockRecipe<?>> extends LycheeRecipe.Serializer<T> {

		public Serializer(Function<ResourceLocation, T> factory) {
			super(factory);
		}

		@Override
		public void fromJson(T pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.input = Ingredient.fromJson(pSerializedRecipe.get("item_in"));
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
		}

		@Override
		public void fromNetwork(T pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.input = Ingredient.fromNetwork(pBuffer);
			pRecipe.block = BlockPredicateHelper.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
			super.toNetwork(pBuffer, pRecipe);
			pRecipe.input.toNetwork(pBuffer);
			BlockPredicateHelper.toNetwork(pRecipe.block, pBuffer);
		}

	}

}
