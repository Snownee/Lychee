package snownee.lychee.util.action;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public final class ActionData {
	public static final Codec<ActionData> CODEC =
			RecordCodecBuilder.create(inst ->
					inst.group(
							LycheeContext.CODEC.fieldOf("context").forGetter(ActionData::getContext),
							Codec.INT.fieldOf("delayedTicks").forGetter(ActionData::getDelayedTicks)
					).apply(inst, ActionData::of));
	@Nullable
	private ILycheeRecipe<?> recipe;
	@Nullable
	private LycheeContext context;
	private int delayedTicks;

	private ActionData(
			@Nullable ILycheeRecipe<?> recipe,
			@Nullable LycheeContext context,
			int delayedTicks
	) {
		this.recipe = recipe;
		this.context = context;
		this.delayedTicks = delayedTicks;
	}

	public static ActionData of(@Nullable LycheeContext context, int delayedTicks) {
		ResourceLocation recipeId = context.getMatchedRecipeId(); //TODO
		ILycheeRecipe<?> recipe = null;
		if (recipeId != null) {
			RecipeHolder<Recipe<?>> holder = (RecipeHolder<Recipe<?>>) CommonProxy.recipe(recipeId);
			if (holder != null && holder.value() instanceof ILycheeRecipe) {
				recipe = (ILycheeRecipe<?>) holder.value();
			}
		}
		return new ActionData(recipe, context, delayedTicks);
	}

	public @Nullable ILycheeRecipe<?> getRecipe() {
		return recipe;
	}

	public void setRecipe(final @Nullable ILycheeRecipe<?> recipe) {
		this.recipe = recipe;
	}

	public @Nullable LycheeContext getContext() {
		return context;
	}

	public void setContext(final @Nullable LycheeContext context) {
		this.context = context;
	}

	public int getDelayedTicks() {
		return delayedTicks;
	}

	public int consumeDelayedTicks() {
		return delayedTicks--;
	}

	public void setDelayedTicks(final int delayedTicks) {
		this.delayedTicks = delayedTicks;
	}

	public void addDelayedTicks(int ticks) {
		this.delayedTicks += ticks;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final ActionData that = (ActionData) o;
		return delayedTicks == that.delayedTicks && Objects.equal(recipe, that.recipe) &&
				Objects.equal(context, that.context);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(recipe, context, delayedTicks);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("recipe", recipe)
				.add("context", context)
				.add("delayedTicks", delayedTicks)
				.toString();
	}
}
