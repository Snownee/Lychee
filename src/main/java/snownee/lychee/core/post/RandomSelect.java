package snownee.lychee.core.post;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.Job;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BoundsHelper;
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
	public void doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		times *= IntBoundsHelper.random(rolls, ctx.getRandom());
		if (times == 0) {
			return;
		}
		if (entries.length == 1) {
			ctx.runtime.jobs.push(new Job(entries[0], times));
			return;
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
			return;
		}
		int[] childTimes = new int[validActions.size()];
		for (int i = 0; i < times; i++) {
			++childTimes[getRandomEntry(ctx.getRandom(), validWeights, totalWeights)];
		}
		for (int i = 0; i < validActions.size(); i++) {
			if (childTimes[i] > 0) {
				ctx.runtime.jobs.push(new Job(validActions.get(i), childTimes[i]));
			}
		}
	}

	private int getRandomEntry(RandomSource random, int[] weights, int totalWeights) {
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
		if (entries.length == 1) {
			return Component.literal("%s × %s".formatted(entries[0].getDisplayName().getString(), BoundsHelper.getDescription(rolls).getString()));
		}
		return LUtil.getCycledItem(List.of(entries), entries[0], 1000).getDisplayName();
	}

	@Environment(EnvType.CLIENT)
	public List<Component> getTooltips(PostAction child) {
		int index = Arrays.asList(entries).indexOf(child);
		List<Component> list = entries.length == 1 ? Lists.newArrayList(getDisplayName()) : child.getBaseTooltips();
		if (index == -1) {
			return list; //TODO nested actions?
		}
		if (entries.length > 1) {
			String chance = LUtil.chance(weights[index] / (float) totalWeight);
			if (rolls == IntBoundsHelper.ONE) {
				list.add(Component.translatable("tip.lychee.randomChance.one", chance).withStyle(ChatFormatting.YELLOW));
			} else {
				list.add(Component.translatable("tip.lychee.randomChance", chance, BoundsHelper.getDescription(rolls)).withStyle(ChatFormatting.YELLOW));
			}
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
		// should not be run, except from old versions
	}

	@Override
	public boolean canRepeat() {
		return canRepeat;
	}

	@Override
	public boolean validate(LycheeRecipe<?> recipe) {
		for (PostAction action : entries) {
			if (!action.validate(recipe)) {
				return false;
			}
		}
		return true;
	}

	public static class Type extends PostActionType<RandomSelect> {

		@Override
		public RandomSelect fromJson(JsonObject o) {
			JsonArray array = o.getAsJsonArray("entries");
			int size = array.size();
			Preconditions.checkArgument(size > 0, "entries can not be empty");
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
		public void toJson(RandomSelect action, JsonObject o) {
			JsonArray entries = new JsonArray(action.entries.length);
			int i = 0;
			for (var entry : action.entries) {
				JsonObject entryJson = entry.toJson();
				if (action.weights[i] != 1) {
					entryJson.addProperty("weight", action.weights[i]);
				}
				entries.add(entryJson);
				++i;
			}
			o.add("entries", entries);
			if (action.rolls != IntBoundsHelper.ONE) {
				o.add("rolls", action.rolls.serializeToJson());
			}
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
			buf.writeVarInt(size);
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
