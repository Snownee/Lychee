package snownee.lychee.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.Lychee;
import snownee.lychee.core.post.Delay.LycheeMarker;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.json.JsonSchema;
import snownee.lychee.util.json.JsonSchema.Anchor;

public class ActionRuntime {

	public boolean doDefault = true;
	public State state = State.RUNNING;
	public final LinkedList<Job> jobs = Lists.newLinkedList();
	public LycheeMarker marker;

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public void enqueue(List<PostAction> actions, int times) {
		jobs.addAll(0, actions.stream().map($ -> new Job($, times)).toList());
	}

	public void run(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		try {
			JsonSchema schema = ILycheeRecipe.recipeSchemas.get(recipe.getId());
			Map<Anchor, String> anchoredObjects = schema == null ? Map.of() : Maps.newHashMap();
			if (schema != null) {
				if (ctx.json == null) {
					ctx.json = schema.buildSkeleton();
					// Lychee.LOGGER.info(ctx.json);
					// serialize anchored objects
					schema.anchors().forEach((p, a) -> {
						if (a.override != null)
							return;
						int index = Integer.parseInt(a.id);
						ItemStack stack = ctx.getItem(index);
						JsonObject e = LUtil.tagToJson(stack.save(new CompoundTag()));
						anchoredObjects.put(a, e.toString());
						JsonElement parent = p.parent().find(ctx.json);
						if (parent.isJsonObject()) {
							parent.getAsJsonObject().add(p.getString(-1), e);
						} else { // is array
							parent.getAsJsonArray().set(p.getInt(-1), e);
						}
					});
					Lychee.LOGGER.info(ctx.json);
					jobs.stream().map($ -> $.action).forEach($ -> $.preApply(recipe, ctx, times));
				}
			}
			while (!jobs.isEmpty()) {
				jobs.pop().apply(recipe, ctx);
				if (ctx.runtime.state != State.RUNNING) {
					break;
				}
			}
			if (ctx.runtime.state == State.RUNNING && jobs.isEmpty()) {
				ctx.runtime.state = State.STOPPED;
			}
			if (schema != null) {
				// deserialize anchored objects
				schema.anchors().forEach((p, a) -> {
					if (a.override != null)
						return;
					int index = Integer.parseInt(a.id);
					JsonElement e = p.find(ctx.json);
					if (!Objects.equals(e.toString(), anchoredObjects.get(a))) {
						CompoundTag tag = LUtil.jsonToTag(e.getAsJsonObject());
						ctx.setItem(index, ItemStack.of(tag));
					}
				});
			}
		} catch (Throwable e) {
			Lychee.LOGGER.error("Error running actions");
			Lychee.LOGGER.catching(e);
			ctx.runtime.state = State.STOPPED;
		}
	}

}
