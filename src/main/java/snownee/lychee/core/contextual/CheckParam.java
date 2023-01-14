package snownee.lychee.core.contextual;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.LUtil;

//TODO
public record CheckParam(String key, Object value) implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.CHECK_PARAM;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		ctx.lazyGetBlockEntity();
		for (LootContextParam<?> param : ctx.getParams().keySet()) {
			if (key.equals(param.getName().getPath()) || key.equals(param.getName().toString())) {
				return times;
			}
		}
		return 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = makeDescriptionId(inverted) + ".has";
		return Component.translatable(key, LUtil.white(key));
	}

	public static class Type extends ContextualConditionType<CheckParam> {

		@Override
		public CheckParam fromJson(JsonObject o) {
			return new CheckParam(o.get("key").getAsString(), null);
		}

		@Override
		public void toJson(CheckParam condition, JsonObject o) {
			o.addProperty("key", condition.key());
		}

		@Override
		public CheckParam fromNetwork(FriendlyByteBuf buf) {
			return new CheckParam(buf.readUtf(), null);
		}

		@Override
		public void toNetwork(CheckParam condition, FriendlyByteBuf buf) {
			buf.writeUtf(condition.key());
		}

	}

}
