package snownee.lychee.core.contextual;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public record Chance(float chance) implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.CHANCE;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		int n = 0;
		for (int i = 0; i < times; i++) {
			if (ctx.getRandom().nextFloat() < chance) {
				++n;
			}
		}
		return n;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = makeDescriptionId(inverted);
		String chance = this.chance < 0.01 ? "<1" : String.valueOf((int) (this.chance * 100));
		return new TranslatableComponent(key, LUtil.white(chance + "%"));
	}

	public static class Type extends ContextualConditionType<Chance> {

		@Override
		public Chance fromJson(JsonObject o) {
			return new Chance(o.get("chance").getAsFloat());
		}

		@Override
		public Chance fromNetwork(FriendlyByteBuf buf) {
			return new Chance(buf.readFloat());
		}

		@Override
		public void toNetwork(Chance condition, FriendlyByteBuf buf) {
			buf.writeFloat(condition.chance);
		}

	}

}
