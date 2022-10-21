package snownee.lychee.item_burning;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class ItemBurningRecipe extends LycheeRecipe<LycheeContext> {

	protected Ingredient input;

	public ItemBurningRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(LycheeContext ctx, Level pLevel) {
		ItemStack stack = ((ItemEntity) ctx.getParam(LootContextParams.THIS_ENTITY)).getItem();
		return input.test(stack);
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return RecipeSerializers.ITEM_BURNING;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.ITEM_BURNING;
	}

	public Ingredient getInput() {
		return input;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
	}

	public static class Serializer extends LycheeRecipe.Serializer<ItemBurningRecipe> {

		public Serializer() {
			super(ItemBurningRecipe::new);
		}

		@Override
		public void fromJson(ItemBurningRecipe pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.input = Ingredient.fromJson(pSerializedRecipe.get("item_in"));
		}

		@Override
		public void fromNetwork(ItemBurningRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.input = Ingredient.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, ItemBurningRecipe pRecipe) {
			pRecipe.input.toNetwork(pBuffer);
		}

	}

	public static void on(ItemEntity entity) {
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(entity.level);
		builder.withParameter(LootContextParams.ORIGIN, entity.position());
		builder.withParameter(LootContextParams.THIS_ENTITY, entity);
		LycheeContext ctx = builder.create(RecipeTypes.ITEM_BURNING.contextParamSet);
		RecipeTypes.ITEM_BURNING.findFirst(ctx, entity.level).ifPresent($ -> {
			int times = $.getRandomRepeats(entity.getItem().getCount(), ctx);
			$.applyPostActions(ctx, times);
			entity.getItem().shrink(times);
		});
	}

}
