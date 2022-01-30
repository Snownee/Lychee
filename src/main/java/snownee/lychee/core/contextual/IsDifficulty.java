package snownee.lychee.core.contextual;

import java.util.List;
import java.util.stream.IntStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public record IsDifficulty(IntImmutableList difficulties) implements ContextualCondition {

	@Override
	public ContextualConditionType<? extends ContextualCondition> getType() {
		return ContextualConditionTypes.DIFFICULTY;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return difficulties.contains(ctx.getLevel().getDifficulty().getId()) ? times : 0;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public InteractionResult testInTooltips() {
		return difficulties.contains(Minecraft.getInstance().level.getDifficulty().getId()) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = makeDescriptionId(inverted);
		List<String> names = difficulties.intParallelStream().mapToObj(Difficulty::byId).map(Difficulty::getDisplayName).map(Component::getString).toList();
		int size = names.size();
		if (size == 1) {
			return new TranslatableComponent(key, LUtil.white(names.get(0)));
		} else {
			StringBuilder sb = new StringBuilder();
			key += ".more";
			for (int i = 0; i < size - 1; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(names.get(i));
			}
			return new TranslatableComponent(key, LUtil.white(sb), LUtil.white(names.get(size - 1)));
		}
	}

	public static class Type extends ContextualConditionType<IsDifficulty> {

		@Override
		public IsDifficulty fromJson(JsonObject o) {
			JsonElement e = o.get("difficulty");
			if (e.isJsonPrimitive()) {
				return new IsDifficulty(IntImmutableList.of(parseDifficulty(e.getAsJsonPrimitive())));
			} else {
				IntArrayList list = IntArrayList.of();
				e.getAsJsonArray().forEach($ -> list.add(parseDifficulty($.getAsJsonPrimitive())));
				return new IsDifficulty(new IntImmutableList(list));
			}
		}

		private static int parseDifficulty(JsonPrimitive e) {
			if (e.isNumber()) {
				return e.getAsInt();
			} else {
				return Difficulty.byName(e.getAsString()).getId();
			}
		}

		@Override
		public IsDifficulty fromNetwork(FriendlyByteBuf buf) {
			int size = buf.readVarInt();
			return new IsDifficulty(IntImmutableList.toList(IntStream.range(0, size).map($ -> buf.readVarInt())));
		}

		@Override
		public void toNetwork(IsDifficulty condition, FriendlyByteBuf buf) {
			buf.writeVarInt(condition.difficulties.size());
			condition.difficulties.forEach(buf::writeVarInt);
		}

	}

}
