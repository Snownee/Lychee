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

public record RemoveComponent(
		PostActionCommonProperties commonProperties,
		List<DataComponentType<?>> component,
		Reference target) implements PostAction {
	public static final Reference DEFAULT_TARGET = new Reference.Pointer(ILycheeRecipe.ITEM_OUT_POINTER);

	@Override
	public boolean hidden() {
		return true;
	}

	@Override
	public PostActionType<?> type() {
		return PostActionTypes.REMOVE_COMPONENT;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		IntList targetIndexes = context.get(LycheeContextKey.RECIPE).getItemIndexes(target);
		ItemStackHolderCollection itemHolders = context.get(LycheeContextKey.ITEM);
		for (int targetIndex : targetIndexes) {
			ItemStack targetItem = itemHolders.get(targetIndex).get();
			for (DataComponentType<?> type : component.isEmpty() ? targetItem.getComponents().keySet() : component) {
				Object sourceComponent = targetItem.get(type);
				if (sourceComponent != null) {
					//noinspection unchecked,rawtypes
					targetItem.set((DataComponentType) type, sourceComponent);
				}
			}
		}
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		var targetIndexes = recipe.getItemIndexes(target);
		if (targetIndexes.isEmpty()) {
			throw new IllegalStateException("No target item for RemoveComponent action: " + target);
		}
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	public static class Type implements PostActionType<RemoveComponent> {
		public static final MapCodec<RemoveComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(RemoveComponent::commonProperties),
						LycheeCodecs.WILDCARD_COMPONENTS.fieldOf("component").forGetter(RemoveComponent::component),
						Reference.CODEC.optionalFieldOf("target", DEFAULT_TARGET).forGetter(RemoveComponent::target))
				.apply(instance, RemoveComponent::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, RemoveComponent> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				RemoveComponent::commonProperties,
				DataComponentType.STREAM_CODEC.apply(ByteBufCodecs.list()),
				RemoveComponent::component,
				Reference.STREAM_CODEC,
				RemoveComponent::target,
				RemoveComponent::new);

		@Override
		public MapCodec<RemoveComponent> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, RemoveComponent> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
