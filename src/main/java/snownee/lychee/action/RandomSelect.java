package snownee.lychee.action;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.action.input.NBTPatch;
import snownee.lychee.util.action.Job;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RandomSelect extends PostAction<RandomSelect> implements CompoundAction {

	public final PostAction<?>[] entries;
	public final int[] weights;
	public final MinMaxBounds.Ints rolls;
	public final boolean canRepeat;
	public final boolean hidden;
	public final boolean preventSync;
	public final int totalWeight;
	public final int emptyWeight;

	public RandomSelect(
			PostAction<?>[] entries,
			int[] weights,
			int totalWeight,
			int emptyWeight,
			MinMaxBounds.Ints rolls
	) {
		Preconditions.checkArgument(entries.length == weights.length);
		this.entries = entries;
		this.weights = weights;
		this.totalWeight = totalWeight;
		this.emptyWeight = emptyWeight;
		this.rolls = rolls;
		canRepeat = Arrays.stream(entries).allMatch(PostAction::repeatable);
		hidden = Arrays.stream(entries).allMatch(PostAction::hidden);
		preventSync = Arrays.stream(entries).allMatch(PostAction::preventSync);
	}

	@Override
	public PostActionType<RandomSelect> getType() {
		return PostActionTypes.RANDOM;
	}

	@Override
	public void doApply(ILycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		times *= IntBoundsHelper.random(rolls, ctx.getRandom());
		if (times == 0) {
			return;
		}
		List<PostAction<?>> validActions = Lists.newArrayList();
		int[] validWeights = new int[entries.length];
		int totalWeights = 0;
		for (int i = 0; i < entries.length; i++) {
			PostAction<?> entry = entries[i];
			if (entry.checkConditions(recipe, ctx, 1) == 1) {
				validWeights[validActions.size()] = weights[i];
				validActions.add(entry);
				totalWeights += weights[i];
			}
		}
		if (validActions.isEmpty()) {
			return;
		}
		totalWeights += emptyWeight;
		int[] childTimes = new int[validActions.size()];
		for (int i = 0; i < times; i++) {
			int index = getRandomEntry(ctx.getRandom(), validWeights, totalWeights);
			if (index >= 0) {
				++childTimes[index];
			}
		}
		for (int i = 0; i < validActions.size(); i++) {
			if (childTimes[i] > 0) {
				ctx.runtime.jobs.push(new Job(validActions.get(i), childTimes[i]));
			}
		}
	}

	private int getRandomEntry(RandomSource random, int[] weights, int totalWeights) {
		int j = random.nextInt(totalWeights);
		for (int i = 0; i < weights.length; i++) {
			j -= weights[i];
			if (j < 0) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void apply(ILycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return Stream.of(entries).map(PostAction::getOutputItems).flatMap(List::stream).toList();
	}

	@Override
	public List<BlockPredicate> getOutputBlocks() {
		return Stream.of(entries).map(PostAction::getOutputBlocks).flatMap(List::stream).toList();
	}

	@Override
	public Component getDisplayName() {
		if (entries.length == 1 && emptyWeight == 0) {
			return Component.literal("%s × %s".formatted(
                entries[0].getDisplayName().getString(),
                BoundsExtensions.getDescription(rolls).getString()
			));
		}
		return CommonProxy.getCycledItem(List.of(entries), entries[0], 1000).getDisplayName();
	}

	@Override
	public boolean repeatable() {
		return canRepeat;
	}

	@Override
	public boolean hidden() {
		return hidden;
	}

	@Override
	public boolean preventSync() {
		return preventSync;
	}

	@Override
	public void validate(ILycheeRecipe recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		for (PostAction action : entries) {
			Preconditions.checkArgument(action.getClass() != NBTPatch.class, "NBTPatch cannot be used in " +
																			 "RandomSelect");
			action.validate(recipe, patchContext);
		}
	}

	@Override
	public void getUsedPointers(ILycheeRecipe recipe, Consumer<JsonPointer> consumer) {
		for (PostAction action : entries) {
			action.getUsedPointers(recipe, consumer);
		}
	}

	@Override
	public JsonElement provideJsonInfo(ILycheeRecipe recipe, JsonPointer pointer, JsonObject recipeObject) {
		int i = 0;
		JsonArray array = new JsonArray();
		for (PostAction action : entries) {
			array.add(action.provideJsonInfo(recipe, pointer.append("entries/" + i), recipeObject));
			i++;
		}
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("entries", array);
		return jsonObject;
	}

	@Override
	public Stream<PostAction> getChildActions() {
		return Arrays.stream(entries);
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
			int emptyWeight = GsonHelper.getAsInt(o, "empty_weight", 0);
			Preconditions.checkArgument(emptyWeight >= 0, "empty_weight should be greater or equal to 0");
			return new RandomSelect(entries, weights, IntStream.of(weights).sum() + emptyWeight, emptyWeight, rolls);
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
			if (action.emptyWeight != 0) {
				o.addProperty("empty_weight", action.emptyWeight);
			}
		}

		@Override
		public RandomSelect fromNetwork(FriendlyByteBuf buf) {
			int totalWeight = buf.readVarInt();
			int emptyWeight = buf.readVarInt();
			int size = buf.readVarInt();
			PostAction[] entries = new PostAction[size];
			int[] weights = new int[size];
			for (int i = 0; i < size; i++) {
				weights[i] = buf.readVarInt();
				entries[i] = PostAction.read(buf);
			}
			return new RandomSelect(entries, weights, totalWeight, emptyWeight, IntBoundsHelper.fromNetwork(buf));
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void toNetwork(RandomSelect action, FriendlyByteBuf buf) {
			buf.writeVarInt(action.totalWeight);
			buf.writeVarInt(action.emptyWeight);
			buf.writeVarInt((int) Stream.of(action.entries).filter(Predicate.not(PostAction::preventSync)).count());
			for (int i = 0; i < action.entries.length; i++) {
				PostAction entry = action.entries[i];
				if (entry.preventSync())
					continue;
				buf.writeVarInt(action.weights[i]);
				PostActionType type = entry.getType();
				CommonProxy.writeRegistryId(LycheeRegistries.POST_ACTION, type, buf);
				type.toNetwork(entry, buf);
				entry.conditionsToNetwork(buf);
			}
			IntBoundsHelper.toNetwork(action.rolls, buf);
		}

	}

}
