package snownee.lychee.core.recipe.type;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public class LycheeRecipeType<C extends LycheeContext, T extends LycheeRecipe<C>> implements RecipeType<T> {
	public final ResourceLocation id;
	public final Class<? extends T> clazz;
	public final LootContextParamSet contextParamSet;
	private boolean empty;
	private boolean requiresClient;
	protected List<T> recipes;
	public boolean compactInputs;

	public static final Component DEFAULT_PREVENT_TIP = new TranslatableComponent("tip.lychee.prevent_default.default").withStyle(ChatFormatting.YELLOW);

	public LycheeRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet contextParamSet) {
		id = new ResourceLocation(Lychee.ID, name);
		this.clazz = clazz;
		this.contextParamSet = contextParamSet == null ? LootContextParamSets.get(id) : contextParamSet;
		Objects.requireNonNull(this.contextParamSet);
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public <D extends Container> Optional<T> tryMatch(Recipe<D> pRecipe, Level pLevel, D pContainer) {
		T lycheeRecipe = (T) pRecipe;
		return pRecipe.matches(pContainer, pLevel) && lycheeRecipe.checkConditions(lycheeRecipe, (C) pContainer, 1) > 0 ? Optional.of(lycheeRecipe) : Optional.empty();
	}

	public List<T> recipes() {
		return recipes;
	}

	public void updateEmptyState() {
		empty = recipes.isEmpty();
	}

	public boolean isEmpty() {
		return empty;
	}

	public void buildCache() {
		Stream<T> stream = LUtil.recipes(this).stream().filter($ -> !$.ghost);
		if (clazz.isAssignableFrom(Comparable.class)) {
			stream = stream.sorted();
		}
		recipes = stream.toList();
	}

	public Optional<T> findFirst(C ctx, Level level) {
		return recipes.stream().flatMap($ -> {
			return tryMatch($, level, ctx).stream();
		}).findFirst();
	}

	public Component getPreventDefaultDescription(LycheeRecipe<?> recipe) {
		return DEFAULT_PREVENT_TIP;
	}

	public void setRequiresClient() {
		requiresClient = true;
	}

	public boolean requiresClient() {
		return requiresClient;
	}

	public static class ValidItemCache {
		private IntSet validItems = IntSets.emptySet();

		public void buildCache(List<? extends Recipe<?>> recipes) {
			validItems = new IntAVLTreeSet(recipes.stream().flatMap($ -> {
				return $.getIngredients().stream();
			}).flatMapToInt($ -> {
				return $.getStackingIds().intStream();
			}).toArray());
		}

		public boolean contains(ItemStack stack) {
			return validItems.contains(StackedContents.getStackingIndex(stack));
		}
	}
}
