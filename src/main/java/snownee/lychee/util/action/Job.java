package snownee.lychee.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import snownee.lychee.Lychee;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public record Job(PostAction action, int times) {
	public static final Codec<Job> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					PostActionType.MAP_CODEC.fieldOf("action").forGetter(Job::action),
					Codec.INT.fieldOf("times").forGetter(Job::times)
			).apply(instance, Job::new));


	public void apply(LycheeContext context) {
		var recipe = context.get(LycheeContextKey.RECIPE);
		var recipeId = context.get(LycheeContextKey.RECIPE_ID);
		var times = action.test(recipe, context, this.times);
		if (times > 0) {
			try {
				action.apply(recipe, context, times);
			} catch (Exception e) {
				Lychee.LOGGER.error("Error when apply post action for recipe {}", recipeId.id(), e);
			}
		} else {
			action.onFailure(recipe, context, this.times);
		}
	}
}
