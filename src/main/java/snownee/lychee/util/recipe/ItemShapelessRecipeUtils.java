package snownee.lychee.util.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.lychee.util.RecipeMatcher;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public class ItemShapelessRecipeUtils {
	public static final int MAX_INGREDIENTS = 27;

	public static boolean matches(LycheeContext context, NonNullList<Ingredient> ingredients) {
		var itemShapelessContext = context.get(LycheeContextKey.ITEM_SHAPELESS);
		if (itemShapelessContext.totalItems < ingredients.size()) {
			return false;
		}
		if (ingredients.isEmpty()) {
			return true;
		}
		final var itemEntities = itemShapelessContext.itemEntities.stream().filter(it -> {
			// ingredient.test is not thread safe
			return ingredients.stream().anyMatch(ingredient -> ingredient.test(it.getItem()));
		}).limit(MAX_INGREDIENTS).toList();
		final var items = itemEntities.stream().map(ItemEntity::getItem).toList();
		final var amount = items.stream().mapToInt(ItemStack::getCount).toArray();
		final var match = RecipeMatcher.findMatches(items, ingredients, amount);
		if (match.isEmpty()) {
			return false;
		}
		itemShapelessContext.filteredItems = itemEntities;
		itemShapelessContext.setMatcher(match.get());
		return true;
	}

	public static <T extends ILycheeRecipe<?>> MapCodec<T> validatedCodec(MapCodec<T> codec) {
		return codec.validate(it -> {
			if (!it.ghost() && it.getIngredients().size() > MAX_INGREDIENTS) {
				return DataResult.error(() -> "Ingredients cannot be more than " + MAX_INGREDIENTS);
			}
			return DataResult.success(it);
		});
	}
}
