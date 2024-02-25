package snownee.lychee.recipes;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

public class ItemExplodingRecipe extends LycheeRecipe<LycheeContext> implements Comparable<ItemExplodingRecipe> {
	public static void invoke(final ServerLevel level, double x, double y, double z, List<Entity> entityList, float radius) {
		final var itemEntities = entityList.stream()
				.filter(it -> it instanceof ItemEntity)
				.map(ItemEntity.class::cast);
		final var context = new LycheeContext();
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.ORIGIN, new Vec3(x, y, z));
		lootParamsContext.setParam(LootContextParams.EXPLOSION_RADIUS, radius);
		RecipeTypes.ITEM_EXPLODING.process(level, itemEntities, context);
	}


	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public ItemExplodingRecipe(LycheeRecipeCommonProperties commonProperties) {
		super(commonProperties);
	}

	public ItemExplodingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			NonNullList<Ingredient> ingredients
	) {
		super(commonProperties);
		this.ingredients = ingredients;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return ItemShapelessRecipeUtils.matches(context, ingredients);
	}

	@Override
	public @NotNull RecipeSerializer<ItemExplodingRecipe> getSerializer() {
		return RecipeSerializers.ITEM_EXPLODING;
	}

	@Override
	public @NotNull RecipeType<ItemExplodingRecipe> getType() {
		return RecipeTypes.ITEM_EXPLODING;
	}

	@Override
	public int compareTo(@NotNull ItemExplodingRecipe that) {
		int i;
		i = Integer.compare(maxRepeats().isAny() ? 1 : 0, that.maxRepeats().isAny() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = -Integer.compare(ingredients.size(), that.ingredients.size());
		return i;
	}
}
