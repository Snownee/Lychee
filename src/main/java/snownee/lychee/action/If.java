package snownee.lychee.action;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class If implements CompoundAction, PostAction {
	public final List<PostAction> successEntries;
	public final List<PostAction> failureEntries;
	private final PostActionCommonProperties commonProperties;
	public final boolean canRepeat;
	public final boolean hidden;
	public final boolean preventSync;

	public If(PostActionCommonProperties commonProperties, List<PostAction> successEntries, List<PostAction> failureEntries) {
		this.commonProperties = commonProperties;
		this.successEntries = successEntries;
		this.failureEntries = failureEntries;
		canRepeat = getChildActions().allMatch(PostAction::repeatable);
		hidden = getChildActions().allMatch(PostAction::hidden);
		preventSync = getChildActions().allMatch(PostAction::preventSync);
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
	public PostActionCommonProperties commonProperties() {
		return commonProperties;
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
	public void getUsedPointers(@Nullable ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {
		for (var action : getChildActions().toList()) {
			action.getUsedPointers(recipe, consumer);
		}
	}

	public static class Type implements PostActionType<If> {
		public static final Codec<If> CODEC = ExtraCodecs.validate(
				RecordCodecBuilder.create(instance -> instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(If::commonProperties),
						new CompactListCodec<>(PostAction.CODEC).fieldOf("then").forGetter(it -> it.successEntries),
						new CompactListCodec<>(PostAction.CODEC).fieldOf("else").forGetter(it -> it.failureEntries)
				).apply(instance, If::new)),
				it -> {
					if (it.successEntries.isEmpty() && it.failureEntries.isEmpty()) {
						return DataResult.error(() -> "Both 'then' and 'else' entries are empty");
					}
					return DataResult.success(it);
				}
		);

		@Override
		public Codec<If> codec() {
			return CODEC;
		}
	}
}
