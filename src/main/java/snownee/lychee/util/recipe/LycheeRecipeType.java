package snownee.lychee.util.recipe;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.mixin.LootContextParamSetsAccess;
import snownee.lychee.util.context.LycheeContext;

public class LycheeRecipeType<T extends ILycheeRecipe<LycheeContext>> implements RecipeType<T> {
	public final Identifier id;
	public Identifier categoryId;
	public final Class<? extends T> recipeClass;
	public final ContextKeySet contextParamSet;
	/**
	 * Ghost recipes not included
	 */
	protected List<RecipeHolder<T>> recipes = List.of();
	public boolean requiresClient;
	public boolean canPreventConsumeInputs;

	private boolean empty = true;

	public static final Component DEFAULT_PREVENT_TIP =
			Component.translatable("tip.lychee.preventDefault.default").withStyle(ChatFormatting.YELLOW);

	public LycheeRecipeType(String name, Class<T> recipeClass, @Nullable ContextKeySet contextParamSet) {
		id = categoryId = Lychee.id(name);
		this.recipeClass = recipeClass;
		this.contextParamSet = contextParamSet == null
				? LootContextParamSetsAccess.registry().get(id)
				: contextParamSet;
		Objects.requireNonNull(this.contextParamSet);
	}

	@Override
	public String toString() {
		return "LycheeRecipeType[" + id + "]";
	}

	public Optional<RecipeHolder<T>> tryMatch(RecipeHolder<T> recipeHolder, Level level, LycheeContext context) {
		final var recipe = recipeHolder.value();
		return recipe.matches(context, level) && recipe.test(context) ? Optional.of(recipeHolder) : Optional.empty();
	}

	public List<RecipeHolder<T>> recipes() {
		return recipes;
	}

	public List<RecipeHolder<T>> inViewerRecipes(RecipeMap recipeMap) {
		return recipeMap.byType(this).stream().filter(it -> !it.value().hideInRecipeViewer()).toList();
	}

	public void updateEmptyState() {
		empty = recipes.isEmpty();
	}

	public boolean isEmpty() {
		return empty;
	}

	@MustBeInvokedByOverriders
	public void refreshCache(RecipeMap recipeMap) {
		recipes = recipeMap.byType(this).stream().filter(it -> !it.value().ghost()).sorted(comparator()).toList();
	}

	public Comparator<RecipeHolder<T>> comparator() {
		return Comparator.comparing(RecipeHolder::value, Comparator.comparing(Recipe::isSpecial));
	}

	public Optional<RecipeHolder<T>> findFirst(LycheeContext context, Level level) {
		return recipes.stream().flatMap(it -> tryMatch(it, level, context).stream()).findFirst();
	}

	public Component getPreventDefaultDescription(T recipe) {
		return DEFAULT_PREVENT_TIP;
	}

}
