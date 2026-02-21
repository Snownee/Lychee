package snownee.lychee.action;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record RandomSelect(
		PostActionCommonProperties commonProperties,
		List<Entry> entries,
		int totalWeight,
		int emptyWeight,
		MinMaxBounds.Ints rolls,
		boolean canRepeat,
		boolean hidden,
		boolean preventSync
) implements CompoundAction, PostAction {
	public RandomSelect(
			PostActionCommonProperties commonProperties,
			List<Entry> entries,
			int emptyWeight,
			MinMaxBounds.Ints rolls) {
		this(
				commonProperties,
				entries,
				entries.stream().mapToInt(it -> it.weight).sum() + emptyWeight,
				emptyWeight,
				rolls,
				entries.stream().allMatch(it -> it.action().repeatable()),
				commonProperties.hidden() || entries.stream().allMatch(it -> it.action().hidden()),
				entries.stream().allMatch(it -> it.action().preventSync()));
		Preconditions.checkArgument(totalWeight > 0, "Total weight must be positive");
	}

	@Override
	public PostActionType<RandomSelect> type() {
		return PostActionTypes.RANDOM;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		var randomSource = context.get(LycheeContextKey.RANDOM);
		times *= BoundsExtensions.random(rolls, randomSource);
		if (times == 0) {
			return;
		}

		var validActions = Lists.<PostAction>newArrayList();
		var validWeights = new int[entries.size()];
		var totalWeights = 0;
		for (var entry : entries) {
			if (entry.action.test(context, actionContext, 1) == 1) {
				validWeights[validActions.size()] = entry.weight;
				validActions.add(entry.action);
				totalWeights += entry.weight;
			}
		}
		if (validActions.isEmpty()) {
			return;
		}
		totalWeights += emptyWeight;
		var childTimes = new int[validActions.size()];
		for (var i = 0; i < times; i++) {
			var index = getRandomEntry(randomSource, validWeights, totalWeights);
			if (index >= 0) {
				++childTimes[index];
			}
		}
		for (var i = 0; i < validActions.size(); i++) {
			if (childTimes[i] > 0) {
				actionContext.jobs.offer(new Job(validActions.get(i), childTimes[i]));
			}
		}
	}

	private int getRandomEntry(RandomSource random, int[] weights, int totalWeights) {
		var j = random.nextInt(totalWeights);
		for (var i = 0; i < weights.length; i++) {
			j -= weights[i];
			if (j < 0) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public List<SlotDisplay> getOutputItems() {
		return entries.stream().map(it -> it.action.getOutputItems()).flatMap(List::stream).toList();
	}

	@Override
	public List<BlockPredicate> getOutputBlocks() {
		return entries.stream().map(it -> it.action.getOutputBlocks()).flatMap(List::stream).toList();
	}

	@Override
	public Component getDisplayName() {
		if (entries.size() == 1 && emptyWeight == 0) {
			return Component.literal("%s × %s".formatted(
					entries.getFirst().action.getDisplayName().getString(),
					BoundsExtensions.getPlainDescription(rolls).getString()
			));
		}
		return CommonProxy.getCycledItem(entries, entries.getFirst(), 1000).action.getDisplayName();
	}

	@Override
	public void getUsedPointers(@Nullable ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {
		for (var entry : entries) {
			entry.action.getUsedPointers(recipe, consumer);
		}
	}

	@Override
	public Stream<PostAction> getChildActions() {
		return entries.stream().map(it -> it.action);
	}

	public record Entry(PostAction action, int weight) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostAction.MAP_CODEC.forGetter(Entry::action),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(Entry::weight)
		).apply(instance, Entry::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				PostAction.STREAM_CODEC,
				Entry::action,
				ByteBufCodecs.VAR_INT,
				Entry::weight,
				Entry::new);
	}

	public static class Type implements PostActionType<RandomSelect> {
		public static final MapCodec<RandomSelect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(RandomSelect::commonProperties),
				ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(Entry.CODEC)).fieldOf("entries").forGetter(RandomSelect::entries),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("empty_weight", 0)
						.forGetter(RandomSelect::emptyWeight),
				MinMaxBounds.Ints.CODEC.optionalFieldOf("rolls", BoundsExtensions.ONE).forGetter(RandomSelect::rolls)
		).apply(instance, RandomSelect::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, RandomSelect> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				RandomSelect::commonProperties,
				Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
				RandomSelect::entries,
				ByteBufCodecs.VAR_INT,
				RandomSelect::emptyWeight,
				MinMaxBounds.Ints.STREAM_CODEC,
				RandomSelect::rolls,
				RandomSelect::new);

		@Override
		public MapCodec<RandomSelect> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, RandomSelect> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
