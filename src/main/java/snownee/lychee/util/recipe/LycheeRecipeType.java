package snownee.lychee.util.recipe;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.mixin.LootContextParamSetsAccess;
import snownee.lychee.util.context.LycheeContext;

public class LycheeRecipeType<T extends ILycheeRecipe<LycheeContext>> implements RecipeType<T> {
	public final ResourceLocation id;
	public ResourceLocation categoryId;
	public final Class<? extends T> clazz;
	public final LootContextParamSet contextParamSet;
	/**
	 * Ghost recipes not included
	 */
	protected List<RecipeHolder<T>> recipes;
	public boolean requiresClient;
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

	public Optional<RecipeHolder<T>> tryMatch(RecipeHolder<T> recipeHolder, Level level, LycheeContext context) {
		final var lycheeRecipe = recipeHolder.value();
		return lycheeRecipe.matches(context, level) &&
				lycheeRecipe.test(recipeHolder.value(), context, 1) > 0
				? Optional.of(recipeHolder)
				: Optional.empty();
	}

	public List<RecipeHolder<T>> recipes() {
		return recipes;
	}

	public List<RecipeHolder<T>> inViewerRecipes() {
		return KUtil.getRecipes(this).stream().filter(it -> !it.value().hideInRecipeViewer()).toList();
	}

	public void updateEmptyState() {
		empty = recipes.isEmpty();
	}

	public boolean isEmpty() {
		return empty;
	}

	@MustBeInvokedByOverriders
	public void refreshCache() {
		recipes = KUtil.getRecipes(this).stream().filter(it -> !it.value().ghost()).sorted(comparator()).toList();
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
