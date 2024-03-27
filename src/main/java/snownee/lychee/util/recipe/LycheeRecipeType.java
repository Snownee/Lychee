package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.kiwi.util.Util;
import snownee.lychee.Lychee;
import snownee.lychee.mixin.LootContextParamSetsAccess;
import snownee.lychee.util.context.LycheeContext;

public class LycheeRecipeType<C extends Container, T extends ILycheeRecipe<C>> implements RecipeType<T> {
	public final ResourceLocation id;
	public ResourceLocation categoryId;
	public final Class<? extends T> clazz;
	public final LootContextParamSet contextParamSet;
	/**
	 * Ghost recipes not included
	 */
	protected List<RecipeHolder<T>> recipes;
	public boolean requiresClient;
	public boolean compactInputs;
	public boolean canPreventConsumeInputs;
	public boolean hasStandaloneCategory = true;

	private boolean empty = true;

	public static final Component DEFAULT_PREVENT_TIP =
			Component.translatable("tip.lychee.preventDefault.default").withStyle(ChatFormatting.YELLOW);

	public LycheeRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet contextParamSet) {
		id = categoryId = Lychee.id(name);
		this.clazz = clazz;
		this.contextParamSet = contextParamSet == null
				? LootContextParamSetsAccess.registry().get(id)
				: contextParamSet;
		Objects.requireNonNull(this.contextParamSet);
	}

	@Override
	public String toString() {
		return "LycheeRecipeType[" + id + "]";
	}

	public Optional<RecipeHolder<T>> tryMatch(RecipeHolder<T> recipeHolder, Level level, C container) {
		if (!(container instanceof LycheeContext context)) {
			// Always return empty if the container is not a LycheeContext
			return Optional.empty();
		}
		final var lycheeRecipe = recipeHolder.value();
		return lycheeRecipe.matches(container, level) &&
				lycheeRecipe.test(recipeHolder.value(), context, 1) > 0
				? Optional.of(recipeHolder)
				: Optional.empty();
	}

	public List<RecipeHolder<T>> recipes() {
		return recipes;
	}

	public List<RecipeHolder<T>> inViewerRecipes() {
		return Util.getRecipes(this).stream().filter(it -> !it.value().hideInRecipeViewer()).toList();
	}

	public void updateEmptyState() {
		empty = recipes.isEmpty();
	}

	public boolean isEmpty() {
		return empty;
	}

	public void refreshCache() {
		var stream = Util.getRecipes(this).stream().filter(it -> !it.value().ghost());
		if (clazz.isAssignableFrom(Comparable.class)) {
			stream = stream.sorted();
		}
		recipes = stream.toList();
	}

	public Optional<RecipeHolder<T>> findFirst(C container, Level level) {
		return recipes.stream().flatMap(it -> tryMatch(it, level, container).stream()).findFirst();
	}

	public Component getPreventDefaultDescription(T recipe) {
		return DEFAULT_PREVENT_TIP;
	}

}
