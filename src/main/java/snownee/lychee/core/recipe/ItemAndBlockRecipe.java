package snownee.lychee.core.recipe;

import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
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
import snownee.lychee.util.LUtil;

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
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
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
		i = Integer.compare(getMaxRepeats().isAny() ? 1 : 0, that.getMaxRepeats().isAny() ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(block == BlockPredicate.ANY ? 1 : 0, that.block == BlockPredicate.ANY ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(LUtil.isSimpleIngredient(input) ? 1 : 0, LUtil.isSimpleIngredient(that.input) ? 1 : 0);
		if (i != 0)
			return i;
		return getId().compareTo(that.getId());
	}

	public static class Serializer<T extends ItemAndBlockRecipe<?>> extends LycheeRecipe.Serializer<T> {

		public Serializer(Function<ResourceLocation, T> factory) {
			super(factory);
		}

		@Override
		public void fromJson(T pRecipe, JsonObject pSerializedRecipe) {
			JsonElement element = pSerializedRecipe.get("item_in");
			if (element instanceof JsonObject object && !object.has("type") && object.has("item") && object.get("item").getAsString().equals("air")) {
				pRecipe.input = Ingredient.of(ItemStack.EMPTY);
			} else {
				pRecipe.input = Ingredient.fromJson(element);
			}
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
