package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.JavaOps;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import snownee.lychee.Lychee;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;

public class LycheeRecipeType<T extends LycheeRecipe<T>> implements RecipeType<T> {
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

	private Supplier<Boolean> isEmpty = Suppliers.memoize(() -> recipes.isEmpty());

	public static final Component DEFAULT_PREVENT_TIP =
			Component.translatable("tip.lychee.preventDefault.default").withStyle(ChatFormatting.YELLOW);

	public LycheeRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet contextParamSet) {
		id = categoryId = name.contains(":") ? new ResourceLocation(name) : new ResourceLocation(Lychee.ID, name);
		this.clazz = clazz;
		this.contextParamSet = contextParamSet == null
							   ? LootContextParamSets.CODEC.parse(JavaOps.INSTANCE, id).result().orElseThrow()
							   : contextParamSet;
		Objects.requireNonNull(this.contextParamSet);
	}

	@Override
	public String toString() {
		return id.toString();
	}

	public Optional<T> tryMatch(RecipeHolder<T> recipeHolder, Level level, LycheeContext context) {
		T lycheeRecipe = recipeHolder.value();
		return lycheeRecipe.matches(context, level) && lycheeRecipe.test(
				(RecipeHolder<LycheeRecipe<?>>) recipeHolder,
				context,
				1
		) > 0
			   ? Optional.of(lycheeRecipe)
			   : Optional.empty();
	}

	public List<RecipeHolder<T>> recipes() {
		return recipes;
	}

	public List<RecipeHolder<T>> inViewerRecipes() {
		return CommonProxy.recipes(this).stream().filter(it -> !it.value().hideInRecipeViewer()).toList();
	}

	public void updateEmptyState() {
		isEmpty = Suppliers.memoize(() -> recipes.isEmpty());
	}

	public boolean isEmpty() {
		return isEmpty.get();
	}

	public void refreshCache() {
		var stream = CommonProxy.recipes(this).stream().filter(it -> !it.value().ghost());
		if (clazz.isAssignableFrom(Comparable.class)) {
			stream = stream.sorted();
		}
		recipes = stream.toList();
	}

	public Optional<T> findFirst(LycheeContext ctx, Level level) {
		return recipes.stream().flatMap(it -> tryMatch(it, level, ctx).stream()).findFirst();
	}

	public Component getPreventDefaultDescription(T recipe) {
		return DEFAULT_PREVENT_TIP;
	}

}
