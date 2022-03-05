package snownee.lychee.core.post;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public class RandomSelect extends PostAction {

	public final PostAction[] entries;
	public final int[] weights;
	public final MinMaxBounds.Ints rolls;
	public final boolean canRepeat;
	public final int totalWeight;

	public RandomSelect(PostAction[] entries, int[] weights, MinMaxBounds.Ints rolls) {
		Preconditions.checkArgument(entries.length == weights.length);
		this.entries = entries;
		this.weights = weights;
		this.rolls = rolls;
		canRepeat = Stream.of(entries).allMatch(PostAction::canRepeat);
		totalWeight = IntStream.of(weights).sum();
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.RANDOM;
	}

	@Override
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		times *= IntBoundsHelper.random(rolls, ctx.getRandom());
		if (times == 0) {
			return true;
		}
		List<PostAction> validActions = Lists.newArrayList();
		int[] validWeights = new int[entries.length];
		int totalWeights = 0;
		for (int i = 0; i < entries.length; i++) {
			PostAction entry = entries[i];
			if (entry.checkConditions(recipe, ctx, 1) == 1) {
				validWeights[validActions.size()] = weights[i];
				validActions.add(entry);
				totalWeights += weights[i];
			}
		}
		if (validActions.isEmpty()) {
			return true;
		}
		int[] childTimes = new int[validActions.size()];
		for (int i = 0; i < times; i++) {
			++childTimes[getRandomEntry(ctx.getRandom(), validWeights, totalWeights)];
		}
		boolean doDefault = true;
		for (int i = 0; i < validActions.size(); i++) {
			if (childTimes[i] > 0) {
				doDefault &= validActions.get(i).doApply(recipe, ctx, childTimes[i]);
			}
		}
		return doDefault;
	}

	private int getRandomEntry(Random random, int[] weights, int totalWeights) {
		if (weights.length == 1) {
			return 0;
		}
		int j = random.nextInt(totalWeights);
		for (int i = 0; i < weights.length; i++) {
			j -= weights[i];
			if (j < 0) {
				return i;
			}
		}
		Lychee.LOGGER.error("Something is wrong!");
		return 0;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return Stream.of(entries).flatMap($ -> $.getOutputItems().stream()).toList();
	}

	@Override
	public Component getDisplayName() {
		return LUtil.getCycledItem(List.of(entries), entries[0], 1000).getDisplayName();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public List<Component> getTooltips() {
		int index = Math.toIntExact((System.currentTimeMillis() / 1000) % entries.length);
		PostAction child = entries[index];
		List<Component> list = Lists.newArrayList(child.getDisplayName());
		String chance = LUtil.chance(weights[index] / (float) totalWeight);
		if (rolls == IntBoundsHelper.ONE) {
			list.add(new TranslatableComponent("tip.lychee.randomChance.one", chance).withStyle(ChatFormatting.YELLOW));
		} else {
			list.add(new TranslatableComponent("tip.lychee.randomChance", chance, IntBoundsHelper.toString(rolls)).withStyle(ChatFormatting.YELLOW));
		}
		int c = showingConditionsCount() + child.showingConditionsCount();
		if (c > 0) {
			list.add(LUtil.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		getConditonTooltips(list, 0);
		child.getConditonTooltips(list, 0);
		return list;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		LUtil.getCycledItem(List.of(entries), entries[0], 1000).render(poseStack, x, y);
	}

	@Override
	public boolean canRepeat() {
		return canRepeat;
	}

	public static class Type extends PostActionType<RandomSelect> {

		@Override
		public RandomSelect fromJson(JsonObject o) {
			JsonArray array = o.getAsJsonArray("entries");
			int size = array.size();
			Preconditions.checkArgument(size > 1, "entries should be more than 1");
			PostAction[] entries = new PostAction[size];
			int[] weights = new int[size];
			for (int i = 0; i < size; i++) {
				JsonObject e = array.get(i).getAsJsonObject();
				weights[i] = GsonHelper.getAsInt(e, "weight", 1);
				Preconditions.checkArgument(weights[i] > 0, "weight should be greater than 0");
				entries[i] = PostAction.parse(e);
			}
			MinMaxBounds.Ints rolls;
			if (o.has("rolls")) {
				rolls = MinMaxBounds.Ints.fromJson(o.get("rolls"));
				Objects.requireNonNull(rolls.getMin(), "min");
				Objects.requireNonNull(rolls.getMax(), "max");
			} else {
				rolls = IntBoundsHelper.ONE;
			}
			return new RandomSelect(entries, weights, rolls);
		}

		@Override
		public RandomSelect fromNetwork(FriendlyByteBuf buf) {
			int size = buf.readVarInt();
			PostAction[] entries = new PostAction[size];
			int[] weights = new int[size];
			for (int i = 0; i < size; i++) {
				weights[i] = buf.readVarInt();
				PostActionType<?> type = LUtil.readRegistryId(LycheeRegistries.POST_ACTION, buf);
				PostAction action = type.fromNetwork(buf);
				action.conditionsFromNetwork(buf);
				entries[i] = action;
			}
			return new RandomSelect(entries, weights, IntBoundsHelper.fromNetwork(buf));
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void toNetwork(RandomSelect action, FriendlyByteBuf buf) {
			int size = action.entries.length;
			for (int i = 0; i < size; i++) {
				buf.writeVarInt(action.weights[i]);
				PostActionType type = action.entries[i].getType();
				LUtil.writeRegistryId(LycheeRegistries.POST_ACTION, type, buf);
				type.toNetwork(action.entries[i], buf);
				action.entries[i].conditionsToNetwork(buf);
			}
			IntBoundsHelper.toNetwork(action.rolls, buf);
		}

	}

}
