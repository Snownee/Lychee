package snownee.lychee.recipes.item_burning;

import org.jetbrains.annotations.NotNull;

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
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.core.recipe.recipe.type.LycheeRecipeType;

public class ItemBurningRecipe extends OldLycheeRecipe<LycheeRecipeContext> {

	protected Ingredient input;

	public ItemBurningRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(LycheeRecipeContext ctx, Level pLevel) {
		ItemStack stack = ((ItemEntity) ctx.getParam(LootContextParams.THIS_ENTITY)).getItem();
		return input.test(stack);
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.ITEM_BURNING;
	}

	@Override
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.ITEM_BURNING;
	}

	public Ingredient getInput() {
		return input;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
	}

	public static class Serializer extends OldLycheeRecipe.Serializer<ItemBurningRecipe> {

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
		LycheeRecipeContext.Builder<LycheeRecipeContext> builder = new LycheeRecipeContext.Builder<>(entity.level());
		builder.withParameter(LootContextParams.ORIGIN, entity.position());
		builder.withParameter(LootContextParams.THIS_ENTITY, entity);
		LycheeRecipeContext ctx = builder.create(RecipeTypes.ITEM_BURNING.contextParamSet);
		RecipeTypes.ITEM_BURNING.findFirst(ctx, entity.level()).ifPresent($ -> {
			int times = $.getRandomRepeats(entity.getItem().getCount(), ctx);
			ctx.itemHolders = ItemStackHolderCollection.InWorld.of(entity);
			$.applyPostActions(ctx, times);
			ctx.itemHolders.postApply(true, times);
		});
	}

}
