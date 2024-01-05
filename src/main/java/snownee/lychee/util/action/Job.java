package snownee.lychee.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;

public record Job(PostAction<?> action, int times) {
	public static final Codec<Job> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					PostAction.CODEC.fieldOf("action").forGetter(Job::action),
					Codec.INT.fieldOf("times").forGetter(Job::times)
			).apply(instance, Job::new));


	public void apply(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx) {
		int times = action.test(recipe, ctx, this.times);
		if (times > 0) {
			action.apply(recipe, ctx, times);
		} else {
			action.onFailure(recipe, ctx, this.times);
		}
	}
}
