package snownee.lychee.util.codec;

import static snownee.lychee.util.recipe.LycheeRecipeSerializer.EMPTY_INGREDIENT;

import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.lychee.mixin.IngredientAccess;

public final class LycheeCodecs {
	public static final Codec<Ingredient> SINGLE_INGREDIENT_CODEC = Ingredient.Value.CODEC.xmap(
			IngredientAccess::construct,
			it -> ((IngredientAccess) (Object) it).getValues()[0]);

	public static final Codec<Pair<Ingredient, Ingredient>> PAIR_INGREDIENT_CODEC =
			Codec.either(
							ExtraCodecs.sizeLimitedList(LycheeCodecs.SINGLE_INGREDIENT_CODEC.listOf(), 2),
							LycheeCodecs.SINGLE_INGREDIENT_CODEC)
					.xmap(it -> {
						if (it.right().isPresent()) {
							return Pair.of(it.right().get(), EMPTY_INGREDIENT);
						}
						var left = it.left().orElseThrow();
						return Pair.of(left.get(0), left.size() > 1 ? left.get(1) : EMPTY_INGREDIENT);
					}, it -> Either.left(List.of(it.getFirst(), it.getSecond())));
}
