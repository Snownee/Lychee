package snownee.lychee.item_inside;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.LycheeCounter;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class ItemInsideRecipe extends ItemAndBlockRecipe<LycheeContext> {

	private int time;

	public ItemInsideRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return RecipeSerializers.ITEM_INSIDE;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.ITEM_INSIDE;
	}

	public int getTime() {
		return time;
	}

	@Override
	public boolean tickOrApply(LycheeContext ctx) {
		if (time == 0) {
			return true;
		}
		LycheeCounter entity = (LycheeCounter) ctx.getParam(LootContextParams.THIS_ENTITY);
		ResourceLocation recipeId = entity.lychee$getRecipeId();
		if (getId().equals(recipeId)) {
			int ticks = entity.lychee$getCount();
			if (ticks >= time) {
				entity.lychee$setRecipeId(null);
				entity.lychee$setCount(0);
				return true;
			}
			entity.lychee$setCount(ticks + 1);
		} else {
			entity.lychee$setRecipeId(getId());
		}
		return false;
	}

	public static class Serializer extends ItemAndBlockRecipe.Serializer<ItemInsideRecipe> {

		public Serializer() {
			super(ItemInsideRecipe::new);
		}

		@Override
		public void fromJson(ItemInsideRecipe pRecipe, JsonObject pSerializedRecipe) {
			super.fromJson(pRecipe, pSerializedRecipe);
			pRecipe.time = GsonHelper.getAsInt(pSerializedRecipe, "time", 0);
		}

		@Override
		public void fromNetwork(ItemInsideRecipe pRecipe, FriendlyByteBuf pBuffer) {
			super.fromNetwork(pRecipe, pBuffer);
			pRecipe.time = pBuffer.readVarInt();
		}

		@Override
		public void toNetwork(FriendlyByteBuf pBuffer, ItemInsideRecipe pRecipe) {
			super.toNetwork(pBuffer, pRecipe);
			pBuffer.writeVarInt(pRecipe.time);
		}

	}

}
