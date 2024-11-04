package snownee.lychee.item_inside;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.LycheeCounter;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_inside.ItemInsideRecipeType.Cache;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.RecipeMatcher;

public class ItemInsideRecipe extends ItemShapelessRecipe<ItemInsideRecipe> implements BlockKeyRecipe<ItemInsideRecipe> {

	private int time;
	protected BlockPredicate block;
	private boolean special;

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
	public boolean tickOrApply(ItemShapelessContext ctx) {
		LycheeCounter entity = (LycheeCounter) ctx.getParam(LootContextParams.THIS_ENTITY);
		if (entity.lychee$getCount() >= time) {
			entity.lychee$setRecipeId(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean matches(ItemShapelessContext ctx, Level pLevel) {
		if (ctx.totalItems < ingredients.size()) {
			return false;
		}
		if (!BlockPredicateHelper.fastMatch(block, ctx)) {
			return false;
		}
		List<ItemEntity> itemEntities = ctx.itemEntities.stream().filter($ -> {
			// ingredient.test is not thread safe
			return ingredients.stream().anyMatch(ingredient -> ingredient.test($.getItem()));
		}).limit(ItemShapelessRecipe.MAX_INGREDIENTS).toList();
		List<ItemStack> items = itemEntities.stream().map(ItemEntity::getItem).toList();
		int[] amount = items.stream().mapToInt(ItemStack::getCount).toArray();
		Optional<RecipeMatcher<ItemStack>> match = RecipeMatcher.findMatches(items, ingredients, amount);
		if (match.isEmpty()) {
			return false;
		}
		ctx.filteredItems = itemEntities;
		ctx.setMatch(match.get());
		return true;
	}

	@Override
	public BlockPredicate getBlock() {
		return block;
	}

	@Nullable
	public Cache buildCache(Object2FloatMap<Item> itemWeights, List<ItemInsideRecipe> specialRecipes) {
		special = !getIngredients().stream().anyMatch(CommonProxy::isSimpleIngredient);
		if (special) {
			specialRecipes.add(this);
			return null;
		}
		/* off */
		var mappedIngredients = getIngredients().stream()
				.map(Ingredient::getItems)
				.map($ -> {
					Set<Item> items = Sets.newHashSet();
					float weight = 1F / $.length;
					for (ItemStack stack : $) {
						items.add(stack.getItem());
						itemWeights.merge(stack.getItem(), weight, Float::sum);
					}
					return items;
				})
				.toList();
		/* on */
		return new Cache(this, mappedIngredients);
	}

	@Override
	public boolean isSpecial() {
		return special;
	}

	public static class Serializer extends ItemShapelessRecipe.Serializer<ItemInsideRecipe> {

		public Serializer() {
			super(ItemInsideRecipe::new);
		}

		@Override
		public void fromJson(ItemInsideRecipe pRecipe, JsonObject pSerializedRecipe) {
			super.fromJson(pRecipe, pSerializedRecipe);
			pRecipe.time = GsonHelper.getAsInt(pSerializedRecipe, "time", 0);
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
			if (!pRecipe.ghost) {
				Preconditions.checkArgument(!pRecipe.ingredients.isEmpty(), "Ingredients cannot be empty");
			}
		}

		@Override
		public void fromNetwork(ItemInsideRecipe pRecipe, FriendlyByteBuf pBuffer) {
			super.fromNetwork(pRecipe, pBuffer);
			pRecipe.time = pBuffer.readVarInt();
			pRecipe.block = BlockPredicateHelper.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, ItemInsideRecipe pRecipe) {
			super.toNetwork0(pBuffer, pRecipe);
			pBuffer.writeVarInt(pRecipe.time);
			BlockPredicateHelper.toNetwork(pRecipe.block, pBuffer);
		}

	}

}
