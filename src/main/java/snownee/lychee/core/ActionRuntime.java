package snownee.lychee.core;

import java.util.LinkedList;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.Lychee;
import snownee.lychee.core.post.Delay.LycheeMarker;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.LUtil;

public class ActionRuntime {

	public boolean doDefault = true;
	public State state = State.RUNNING;
	public final LinkedList<Job> jobs = Lists.newLinkedList();
	public LycheeMarker marker;

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	void enqueue(Stream<PostAction> actions, int times) {
		jobs.addAll(0, actions.map($ -> new Job($, times)).toList());
	}

	public void run(ILycheeRecipe<?> recipe, LycheeContext ctx) {
		ILycheeRecipe.NBTPatchContext patchContext = ILycheeRecipe.patchContexts.get(recipe.lychee$getId());
		if (patchContext != null && ctx.json == null) {
			ctx.json = patchContext.template().deepCopy();
			for (Integer index : patchContext.usedIndexes()) {
				ItemStack item = ctx.getItem(index);
				ctx.json.add(index.toString(), LUtil.tagToJson(item.save(new CompoundTag())));
			}
			//			Lychee.LOGGER.info(ctx.json);
			jobs.forEach($ -> $.action.preApply(recipe, ctx, patchContext));
			//				Lychee.LOGGER.info(ctx.json);
			for (Integer index : patchContext.usedIndexes()) {
				try {
					CompoundTag tag = LUtil.jsonToTag(ctx.json.getAsJsonObject(Integer.toString(index)));
					//						System.out.println(ItemStack.of(tag).getTag());
					ctx.setItem(index, ItemStack.of(tag));
				} catch (Throwable e) {
					Lychee.LOGGER.error("Error parsing json result into item " + ctx.json, e);
					ctx.runtime.state = State.STOPPED;
				}
			}
		}

		try {
			while (!jobs.isEmpty()) {
				jobs.pop().apply(recipe, ctx);
				if (ctx.runtime.state != State.RUNNING) {
					break;
				}
			}
			if (ctx.runtime.state == State.RUNNING || jobs.isEmpty()) {
				ctx.runtime.state = State.STOPPED;
			}
		} catch (Throwable e) {
			Lychee.LOGGER.error("Error running actions", e);
			ctx.runtime.state = State.STOPPED;
		}
	}

}
