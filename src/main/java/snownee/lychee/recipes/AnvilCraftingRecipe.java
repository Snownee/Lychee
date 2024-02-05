package snownee.lychee.recipes;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.codec.ProductExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ConditionHolder;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualByCommonHolder;
import snownee.lychee.util.contextual.ContextualCommonHolder;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeByCommonHolder;
import snownee.lychee.util.recipe.LycheeRecipeCommonHolder;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public final class AnvilCraftingRecipe implements LycheeRecipe<AnvilCraftingRecipe>,
												  LycheeRecipeByCommonHolder<AnvilCraftingRecipe>,
												  Contextual<AnvilCraftingRecipe>,
												  ContextualByCommonHolder<AnvilCraftingRecipe> {
	protected final LycheeRecipeCommonHolder recipeCommonHolder;
	protected final ContextualCommonHolder contextualCommonHolder;
	protected final Pair<Ingredient, Ingredient> input;
	protected final int levelCost;
	protected final int materialCost;
	protected final ItemStack output;
	protected final List<PostAction<?>> assemblingActions;

	public AnvilCraftingRecipe(
			LycheeRecipeCommonHolder recipeCommonHolder,
			ContextualCommonHolder contextualCommonHolder,
			Pair<Ingredient, Ingredient> input,
			int levelCost,
			int materialCost,
			ItemStack output,
			List<PostAction<?>> assemblingActions
	) {
		this.recipeCommonHolder = recipeCommonHolder;
		this.contextualCommonHolder = contextualCommonHolder;
		this.input = input;
		this.levelCost = levelCost;
		this.materialCost = materialCost;
		this.output = output;
		this.assemblingActions = assemblingActions;
	}

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
		final var anvilContext = context.get(LycheeContextKey.ANVIL);
		if (anvilContext == null) return false;
		if (!input.getSecond().isEmpty() && anvilContext.input().getSecond().getCount() < materialCost) return false;
		return input.getFirst().test(anvilContext.input().getFirst())
			   && input.getSecond().test(anvilContext.input().getSecond());
	}

	@Override
	public @NotNull ItemStack assemble(final LycheeContext context, final RegistryAccess registryAccess) {
		final var anvilContext = context.get(LycheeContextKey.ANVIL);
		anvilContext.setLevelCost(levelCost);
		anvilContext.setMaterialCost(materialCost);
		context.get(LycheeContextKey.ITEM).replace(2, getResultItem(registryAccess));
		final var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.reset();
		actionContext.jobs.addAll(assemblingActions.stream().map(it -> new Job(it, 1)).toList());
		actionContext.run(this, context);
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
				RecordCodecBuilder.create(instance ->
						ProductExtensions.and(
								LycheeRecipeSerializer.applyCommonCodecs(instance),
								Codec.either(Codec.pair(Ingredient.CODEC, Ingredient.CODEC), Ingredient.CODEC)
									 .fieldOf(ITEM_IN)
									 .xmap(it -> {
										 if (it.right().isPresent()) {
											 return Pair.of(it.right().get(), EMPTY_INGREDIENT);
										 }
										 return it.left().orElseThrow();
									 }, Either::left)
									 .forGetter(AnvilCraftingRecipe::input),
								PostActionType.LIST_CODEC.optionalFieldOf("assembling", List.of())
														 .forGetter(AnvilCraftingRecipe::assemblingActions),
								ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf(ITEM_OUT)
															   .forGetter(AnvilCraftingRecipe::output),
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

	@Override
	public LycheeRecipeCommonHolder recipeCommonHolder() {return recipeCommonHolder;}

	@Override
	public ContextualCommonHolder contextualCommonHolder() {return contextualCommonHolder;}

	public Pair<Ingredient, Ingredient> input() {return input;}

	public int levelCost() {return levelCost;}

	public int materialCost() {return materialCost;}

	public ItemStack output() {return output;}

	public List<PostAction<?>> assemblingActions() {return assemblingActions;}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final AnvilCraftingRecipe that = (AnvilCraftingRecipe) o;
		return levelCost == that.levelCost && materialCost == that.materialCost && Objects.equal(
				recipeCommonHolder,
				that.recipeCommonHolder
		) && Objects.equal(contextualCommonHolder, that.contextualCommonHolder) &&
			   Objects.equal(input, that.input) && Objects.equal(
				output,
				that.output
		) && Objects.equal(assemblingActions, that.assemblingActions);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
				recipeCommonHolder,
				contextualCommonHolder,
				input,
				levelCost,
				materialCost,
				output,
				assemblingActions
		);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
						  .add("recipeCommonHolder", recipeCommonHolder)
						  .add("contextualCommonHolder", contextualCommonHolder)
						  .add("input", input)
						  .add("levelCost", levelCost)
						  .add("materialCost", materialCost)
						  .add("output", output)
						  .add("assemblingActions", assemblingActions)
						  .toString();
	}
}
