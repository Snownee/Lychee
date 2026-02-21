package snownee.lychee.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import snownee.lychee.Lychee;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public record Job(PostAction action, int times) {
	public static final Codec<Job> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					PostAction.MAP_CODEC.fieldOf("action").forGetter(Job::action),
					Codec.INT.fieldOf("times").forGetter(Job::times)
			).apply(instance, Job::new));

	public void apply(LycheeContext context, ActionContext actionContext) {
		var times = action.test(context, actionContext, this.times);
		if (times > 0) {
			try {
				action.apply(context, actionContext, times);
			} catch (Exception e) {
				Lychee.LOGGER.error("Error when apply post action for recipe {}", context.getOrNull(LycheeContextKey.RECIPE_ID), e);
			}
		} else {
			action.onFailure(context, actionContext, this.times);
		}
	}
}
