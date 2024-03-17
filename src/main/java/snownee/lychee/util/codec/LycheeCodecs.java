package snownee.lychee.util.codec;

import static snownee.lychee.util.recipe.LycheeRecipeSerializer.EMPTY_INGREDIENT;

import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.lychee.mixin.IngredientAccess;

public final class LycheeCodecs {
	public static final Codec<Ingredient> SINGLE_INGREDIENT_CODEC = Ingredient.Value.CODEC.flatComapMap(
			IngredientAccess::construct,
			it -> {
				if (it.isEmpty()) {
					return DataResult.error(() -> "No ingredient found");
				}
				var values = ((IngredientAccess) (Object) it).getValues();
				return DataResult.success(values[0]);
			});

	public static final Codec<Pair<Ingredient, Ingredient>> PAIR_INGREDIENT_CODEC =
			Codec.either(
							ExtraCodecs.sizeLimitedList(ExtraCodecs.nonEmptyList(LycheeCodecs.SINGLE_INGREDIENT_CODEC.listOf()), 2),
							LycheeCodecs.SINGLE_INGREDIENT_CODEC)
					.xmap(it -> {
						if (it.right().isPresent()) {
							return Pair.of(it.right().get(), EMPTY_INGREDIENT);
						}
						var left = it.left().orElseThrow();
						return Pair.of(left.get(0), left.size() > 1 ? left.get(1) : EMPTY_INGREDIENT);
					}, it -> Either.left(Util.make(Lists.newArrayList(it.getFirst()), (list) -> {
						if (!it.getSecond().isEmpty()) {
							list.add(it.getSecond());
						}
					})));

	public static final Codec<ItemStack> PLAIN_ITEM_STACK_CODEC = Codec.either(
					ItemStack.OPTIONAL_CODEC,
					BuiltInRegistries.ITEM.holderByNameCodec().xmap(ItemStack::new, ItemStack::getItemHolder))
			.xmap(it -> it.map(Function.identity(), Function.identity()), Either::left);
}
