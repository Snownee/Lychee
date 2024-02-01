package snownee.lychee.recipes;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextTypes;
import snownee.lychee.util.contextual.ContextualByCommonHolder;
import snownee.lychee.util.contextual.ContextualCommonHolder;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeByCommonHolder;
import snownee.lychee.util.recipe.LycheeRecipeCommonHolder;

public record AnvilCraftingRecipe(
		LycheeRecipeCommonHolder recipeCommonHolder,
		ContextualCommonHolder contextualCommonHolder,
		Pair<Ingredient, Ingredient> input,
		int levelCost,
		int materialCost,
		ItemStack output,
		List<PostAction<?>> assemblingActions,
		ContextualCommonHolder conditionsHolder
) implements LycheeRecipe<AnvilCraftingRecipe>,
			 ContextualByCommonHolder<AnvilCraftingRecipe>,
			 LycheeRecipeByCommonHolder<AnvilCraftingRecipe> {

	@Override
	public IntList getItemIndexes(final JsonPointer pointer) {
		if (pointer.size() == 1) {
			if (pointer.getString(0).equals(ITEM_OUT)) {
				return IntList.of(2);
			}
			if (pointer.getString(0).equals(ITEM_IN)) {
				return input.getSecond().isEmpty() ? IntList.of(0) : IntList.of(0, 1);
			}
		}
		if (pointer.size() == 2 && pointer.getString(0).equals(ITEM_IN)) {
			try {
				int i = pointer.getInt(1);
				if (i >= 0 && i < 2) {
					return IntList.of(i);
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return IntList.of();
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		final var anvilContext = context.get(LycheeContextTypes.ANVIL);
		if (anvilContext == null) return false;
		if (!input.getSecond().isEmpty() && anvilContext.input().getSecond().getCount() < materialCost) return false;
		return input.getFirst().test(anvilContext.input().getFirst())
			   && input.getSecond().test(anvilContext.input().getSecond());
	}

	@Override
	public @NotNull ItemStack assemble(final LycheeContext context, final RegistryAccess registryAccess) {
		final var anvilContext = context.get(LycheeContextTypes.ANVIL);
		anvilContext.setLevelCost(levelCost);
		anvilContext.setMaterialCost(materialCost);
		context.get(LycheeContextTypes.ITEM).items().replace(2, getResultItem(registryAccess));
		final var actionContext = context.get(LycheeContextTypes.ACTION);
		actionContext.reset();
		actionContext.jobs.addAll(assemblingActions.stream().map(it -> new Job(it, 1)).toList());
		actionContext.run(new RecipeHolder<>(id(), this), context);
		return context.getItem(2);
	}

	@Override
	public @NotNull ItemStack getResultItem(final RegistryAccess registryAccess) {
		return output.copy();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		if (input.getSecond().isEmpty()) {
			return NonNullList.of(Ingredient.EMPTY, input.getFirst());
		}
		return NonNullList.of(Ingredient.EMPTY, input.getFirst(), input.getSecond());
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null;
	}

	@Override
	public @NotNull RecipeType<AnvilCraftingRecipe> getType() {
		return null;
	}

	@Override
	public Codec<AnvilCraftingRecipe> contextualCodec() {
		return null;
	}

	@Override
	public List<PostAction<?>> allActions() {
		return Streams.concat(postActions().stream(), assemblingActions().stream()).toList();
	}

	public static class Serializer extends
}
