package snownee.lychee.action.input;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record CopyComponent(
		PostActionCommonProperties commonProperties,
		List<DataComponentType<?>> component,
		Reference source,
		Reference target) implements PostAction {
	public static final Reference DEFAULT_SOURCE = Reference.create("/%s/0".formatted(ILycheeRecipe.ITEM_IN));
	public static final Reference DEFAULT_TARGET = new Reference.Pointer(ILycheeRecipe.ITEM_OUT_POINTER);

	@Override
	public boolean hidden() {
		return true;
	}

	@Override
	public PostActionType<?> type() {
		return PostActionTypes.COPY_COMPONENT;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		ILycheeRecipe<?> recipe = context.get(LycheeContextKey.RECIPE);
		IntList targetIndexes = recipe.getItemIndexes(target);
		ItemStackHolderCollection itemHolders = context.get(LycheeContextKey.ITEM);
		ItemStack sourceItem = itemHolders.get(recipe.getItemIndexes(source).getFirst()).get();
		for (int targetIndex : targetIndexes) {
			ItemStack targetItem = itemHolders.get(targetIndex).get();
			for (DataComponentType<?> type : component.isEmpty() ? sourceItem.getComponents().keySet() : component) {
				Object sourceComponent = sourceItem.get(type);
				if (sourceComponent != null) {
					//noinspection unchecked,rawtypes
					targetItem.set((DataComponentType) type, sourceComponent);
				}
			}
		}
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		var sourceIndexes = recipe.getItemIndexes(source);
		if (sourceIndexes.isEmpty()) {
			throw new IllegalStateException("No source item for CopyComponent action: " + source);
		}
		if (sourceIndexes.size() > 1) {
			throw new IllegalStateException("Multiple source items for CopyComponent action: " + source);
		}
		var targetIndexes = recipe.getItemIndexes(target);
		if (targetIndexes.isEmpty()) {
			throw new IllegalStateException("No target item for CopyComponent action: " + target);
		}
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	public static class Type implements PostActionType<CopyComponent> {
		public static final MapCodec<CopyComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(CopyComponent::commonProperties),
						LycheeCodecs.WILDCARD_COMPONENTS.fieldOf("component").forGetter(CopyComponent::component),
						Reference.CODEC.optionalFieldOf("source", DEFAULT_SOURCE).forGetter(CopyComponent::source),
						Reference.CODEC.optionalFieldOf("target", DEFAULT_TARGET).forGetter(CopyComponent::target))
				.apply(instance, CopyComponent::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, CopyComponent> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				CopyComponent::commonProperties,
				DataComponentType.STREAM_CODEC.apply(ByteBufCodecs.list()),
				CopyComponent::component,
				Reference.STREAM_CODEC,
				CopyComponent::source,
				Reference.STREAM_CODEC,
				CopyComponent::target,
				CopyComponent::new);

		@Override
		public MapCodec<CopyComponent> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CopyComponent> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
