package snownee.lychee.util.action;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.LycheeRecipe;

public final class ActionData {
	public static final Codec<ActionData> CODEC =
			RecordCodecBuilder.create(inst ->
					inst.group(
							ResourceLocation.CODEC.comapFlatMap(it -> {
								final var recipe = CommonProxy.recipe(it);
								if (recipe != null && recipe.value() instanceof LycheeRecipe<?>) {
									return DataResult.success((RecipeHolder<LycheeRecipe<?>>) recipe);
								} else {
									return DataResult.error(() -> it + " is not a Lychee recipe");
								}
							}, RecipeHolder::id).fieldOf("recipe").forGetter(ActionData::getRecipe),
							LycheeContext.CODEC.fieldOf("context").forGetter(ActionData::getContext),
							Codec.INT.fieldOf("delayedTicks").forGetter(ActionData::getDelayedTicks)
					).apply(inst, ActionData::new));
	@Nullable private RecipeHolder<LycheeRecipe<?>> recipe;
	@Nullable private LycheeContext context;
	private int delayedTicks;

	public ActionData(
			@Nullable RecipeHolder<LycheeRecipe<?>> recipe,
			@Nullable LycheeContext context,
			int delayedTicks
	) {
		this.recipe = recipe;
		this.context = context;
		this.delayedTicks = delayedTicks;
	}

	public @Nullable RecipeHolder<LycheeRecipe<?>> getRecipe() {
		return recipe;
	}

	public void setRecipe(final @Nullable RecipeHolder<LycheeRecipe<?>> recipe) {
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
