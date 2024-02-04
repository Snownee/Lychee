package snownee.lychee.recipes;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.contextual.ConditionHolder;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualByCommonHolder;
import snownee.lychee.util.contextual.ContextualCommonHolder;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeByCommonHolder;
import snownee.lychee.util.recipe.LycheeRecipeCommonHolder;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public record AnvilCraftingRecipe(
		LycheeRecipeCommonHolder recipeCommonHolder,
		ContextualCommonHolder contextualCommonHolder,
		Pair<Ingredient, Ingredient> input,
		int levelCost,
		int materialCost,
		ItemStack output,
		List<PostAction<?>> assemblingActions
) implements LycheeRecipe<AnvilCraftingRecipe>,
			 LycheeRecipeByCommonHolder<AnvilCraftingRecipe>,
			 Contextual<AnvilCraftingRecipe>,
			 ContextualByCommonHolder<AnvilCraftingRecipe> {
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
		final var anvilContext = context.get(LycheeContextType.ANVIL);
		if (anvilContext == null) return false;
		if (!input.getSecond().isEmpty() && anvilContext.input().getSecond().getCount() < materialCost) return false;
		return input.getFirst().test(anvilContext.input().getFirst())
			   && input.getSecond().test(anvilContext.input().getSecond());
	}

	@Override
	public @NotNull ItemStack assemble(final LycheeContext context, final RegistryAccess registryAccess) {
		final var anvilContext = context.get(LycheeContextType.ANVIL);
		anvilContext.setLevelCost(levelCost);
		anvilContext.setMaterialCost(materialCost);
		context.get(LycheeContextType.ITEM).items().replace(2, getResultItem(registryAccess));
		final var actionContext = context.get(LycheeContextType.ACTION);
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
	public @NotNull NonNullList<Ingredient> getIngredients() {
		if (input.getSecond().isEmpty()) {
			return NonNullList.of(Ingredient.EMPTY, input.getFirst());
		}
		return NonNullList.of(Ingredient.EMPTY, input.getFirst(), input.getSecond());
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.ANVIL_CRAFTING;
	}

	@Override
	public @NotNull RecipeType<AnvilCraftingRecipe> getType() {
		return RecipeTypes.ANVIL_CRAFTING;
	}

	@Override
	public Stream<PostAction<?>> allActions() {
		return Streams.concat(postActions().stream(), assemblingActions().stream());
	}

	public static class Serializer implements LycheeRecipeSerializer<AnvilCraftingRecipe> {
		public static final Codec<AnvilCraftingRecipe> CODEC =
				RecordCodecBuilder.create(instance -> instance.group(
						LycheeRecipeSerializer.hideInRecipeViewerCodec(),
						LycheeRecipeSerializer.ghostCodec(),
						LycheeRecipeSerializer.commentCodec(),
						LycheeRecipeSerializer.groupCodec(),
						LycheeRecipeSerializer.conditionsCodec(),
						LycheeRecipeSerializer.postActionsCodec(),
						LycheeRecipeSerializer.maxRepeatsCodec(),
						Codec.either(Codec.pair(Ingredient.CODEC, Ingredient.CODEC), Ingredient.CODEC)
							 .fieldOf(ITEM_IN)
							 .xmap(it -> {
								 if (it.right().isPresent()) {
									 return Pair.of(it.right().get(), EMPTY_INGREDIENT);
								 }
								 return it.left().orElseThrow();
							 }, Either::left)
							 .forGetter(AnvilCraftingRecipe::input),
						PostActionType.LIST_CODEC
							 .optionalFieldOf("assembling", List.of())
							 .forGetter(AnvilCraftingRecipe::assemblingActions),
						ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf(ITEM_OUT).forGetter(AnvilCraftingRecipe::output),
						ExtraCodecs.validate(Codec.INT.optionalFieldOf("level_cost", 1), it -> {
							if (it <= 0) {
								return DataResult.error(() -> "level_cost must be greater than 0");
							}
							return DataResult.success(it);
						}).forGetter(AnvilCraftingRecipe::levelCost),
						Codec.INT.optionalFieldOf("material_cost", 1)
								 .forGetter(AnvilCraftingRecipe::materialCost)
				).apply(instance, AnvilCraftingRecipe::of));

		@Override
		public @NotNull Codec<AnvilCraftingRecipe> codec() {
			return CODEC;
		}
	}

	private static AnvilCraftingRecipe of(
			Boolean hideInRecipeViewer,
			Boolean ghost,
			@Nullable String comment,
			String group,
			List<ConditionHolder<?>> conditions,
			List<PostAction<?>> postActions,
			MinMaxBounds.Ints maxRepeats,
			Pair<Ingredient, Ingredient> input,
			List<PostAction<?>> assembling,
			ItemStack output,
			int levelCost,
			int materialCost
	) {
		return new AnvilCraftingRecipe(
				new LycheeRecipeCommonHolder(
						hideInRecipeViewer,
						ghost,
						comment,
						group,
						postActions,
						maxRepeats
				),
				new ContextualCommonHolder(conditions),
				input, levelCost, materialCost, output, assembling
		);
	}
}
