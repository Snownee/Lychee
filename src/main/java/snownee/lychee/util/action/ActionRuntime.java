package snownee.lychee.util.action;

import java.util.Collection;
import java.util.Queue;

import com.google.common.collect.Queues;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.OldLycheeRecipe;

public class ActionRuntime {
	public boolean doDefault = true;
	public State state = State.RUNNING;
	public final Queue<Job> jobs = Queues.newLinkedBlockingQueue();
	public ActionMarker marker;

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public void enqueue(Collection<PostAction<?>> actions, int times) {
		for (final var action : actions) {
			jobs.offer(new Job(action, times));
		}
	}

	public void run(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx) {
		LycheeRecipe.NBTPatchContext patchContext = LycheeRecipe.patchContexts.get(recipe.id());
		if (patchContext != null && ctx.json == null) {
			ctx.json = patchContext.template().deepCopy();
			for (Integer index : patchContext.usedIndexes()) {
				ItemStack item = ctx.getItem(index);
				ctx.json.add(index.toString(), CommonProxy.tagToJson(item.save(new CompoundTag())));
			}
			for (Integer index : patchContext.usedIndexes()) {
				try {
					CompoundTag tag = CommonProxy.jsonToTag(ctx.json.get(Integer.toString(index)));
					//						System.out.println(ItemStack.of(tag).getTag());
					ctx.setItem(index, ItemStack.of(tag));
				} catch (Throwable e) {
					Lychee.LOGGER.error("Error parsing json result into item " + ctx.json, e);
					ctx.runtime.state = State.STOPPED;
				}
			}
		}

		while (!jobs.isEmpty()) {
			final var job = jobs.poll();
			try {
				job.apply(recipe, ctx);
				if (ctx.runtime.state != State.RUNNING) {
					break;
				}
			} catch (Throwable e) {
				// TODO 需要检查这里提供的类型是否可读
				Lychee.LOGGER.error("Error running action {}", job.action().type(), e);
				ctx.runtime.state = State.STOPPED;
			}
		}
		if (ctx.runtime.state == State.RUNNING || jobs.isEmpty()) {
			ctx.runtime.state = State.STOPPED;
		}
	}

}
