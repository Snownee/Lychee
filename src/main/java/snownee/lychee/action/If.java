package snownee.lychee.action;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
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

public record If(
		PostActionCommonProperties commonProperties,
		List<PostAction> successEntries,
		List<PostAction> failureEntries,
		boolean canRepeat,
		boolean hidden,
		boolean preventSync) implements CompoundAction, PostAction {

	public static If of(
			PostActionCommonProperties commonProperties,
			List<PostAction> successEntries,
			List<PostAction> failureEntries) {
		boolean canRepeat = Stream.concat(successEntries.stream(), failureEntries.stream()).allMatch(PostAction::repeatable);
		boolean hidden = commonProperties.hidden() || Stream.concat(successEntries.stream(), failureEntries.stream())
				.allMatch(PostAction::hidden);
		boolean preventSync = Stream.concat(successEntries.stream(), failureEntries.stream()).allMatch(PostAction::preventSync);
		return new If(commonProperties, successEntries, failureEntries, canRepeat, hidden, preventSync);
	}

	public void getConsequenceTooltips(List<Component> list, List<PostAction> actions, String translation) {
		if (actions.isEmpty()) {
			return;
		}
		if (conditions().showingCount() > 0) {
			list.add(Component.translatable(translation).withStyle(ChatFormatting.GRAY));
		}
		for (PostAction child : actions) {
			if (child.hidden()) {
				continue;
			}
			list.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(child.getDisplayName()));
		}
	}

	@Override
	public Stream<PostAction> getChildActions() {
		return Stream.concat(successEntries.stream(), failureEntries.stream());
	}

	@Override
	public PostActionType<If> type() {
		return PostActionTypes.IF;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		for (PostAction action : successEntries) {
			context.get(LycheeContextKey.ACTION).jobs.offer(new Job(action, times));
		}
	}

	@Override
	public void onFailure(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		for (PostAction action : failureEntries) {
			context.get(LycheeContextKey.ACTION).jobs.offer(new Job(action, times));
		}
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return getChildActions().map(PostAction::getOutputItems).flatMap(List::stream).toList();
	}

	@Override
	public List<BlockPredicate> getOutputBlocks() {
		return getChildActions().map(PostAction::getOutputBlocks).flatMap(List::stream).toList();
	}

	@Override
	public void getUsedPointers(@Nullable ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {
		for (var action : getChildActions().toList()) {
			action.getUsedPointers(recipe, consumer);
		}
	}

	public static class Type implements PostActionType<If> {
		public static final MapCodec<If> CODEC =
				RecordCodecBuilder.<If>mapCodec(instance -> instance.group(
								PostActionCommonProperties.MAP_CODEC.forGetter(If::commonProperties),
								PostAction.LIST_CODEC.fieldOf("then").forGetter(If::successEntries),
								PostAction.LIST_CODEC.fieldOf("else").forGetter(If::failureEntries)
						).apply(instance, If::of)
				).validate(
						it -> {
							if (it.successEntries.isEmpty() && it.failureEntries.isEmpty()) {
								return DataResult.error(() -> "Both 'then' and 'else' entries are empty");
							}
							return DataResult.success(it);
						}
				);
		public static final StreamCodec<RegistryFriendlyByteBuf, If> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				If::commonProperties,
				PostAction.STREAM_LIST_CODEC,
				If::successEntries,
				PostAction.STREAM_LIST_CODEC,
				If::failureEntries,
				If::of);

		@Override
		public MapCodec<If> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, If> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
