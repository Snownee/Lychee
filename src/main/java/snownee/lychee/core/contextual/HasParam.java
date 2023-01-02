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

public record HasParam(String name) implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.HAS_PARAM;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (LootContextParam<?> param : ctx.getParams().keySet()) {
			if (name.equals(param.getName().getPath()) || name.equals(param.getName().toString())) {
				return times;
			}
		}
		return 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = makeDescriptionId(inverted);
		return Component.translatable(key, LUtil.white(name));
	}

	public static class Type extends ContextualConditionType<HasParam> {

		@Override
		public HasParam fromJson(JsonObject o) {
			return new HasParam(o.get("name").getAsString());
		}

		@Override
		public void toJson(HasParam condition, JsonObject o) {
			o.addProperty("name", condition.name());
		}

		@Override
		public HasParam fromNetwork(FriendlyByteBuf buf) {
			return new HasParam(buf.readUtf());
		}

		@Override
		public void toNetwork(HasParam condition, FriendlyByteBuf buf) {
			buf.writeUtf(condition.name());
		}

	}

}
