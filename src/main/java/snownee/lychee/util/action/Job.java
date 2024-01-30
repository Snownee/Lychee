package snownee.lychee.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.LycheeRecipe;

public record Job(PostAction<?> action, int times) {
	public static final Codec<Job> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					PostAction.CODEC.fieldOf("action").forGetter(Job::action),
					Codec.INT.fieldOf("times").forGetter(Job::times)
			).apply(instance, Job::new));


	public void apply(RecipeHolder<LycheeRecipe> recipe, LycheeContext context) {
		int times = action.test(recipe, context, this.times);
		if (times > 0) {
			action.apply(recipe, context, times);
		} else {
			action.onFailure(recipe, context, this.times);
		}
	}
}
