package snownee.lychee.action.input;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ExtendedItemStackHolder;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SetItem(PostActionCommonProperties commonProperties, ItemStack itemStack, Reference target) implements PostAction {

	@Override
	public PostActionType<SetItem> type() {
		return PostActionTypes.SET_ITEM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var indexes = Objects.requireNonNull(recipe).getItemIndexes(target);
		var registryAccess = context.level().registryAccess();
		var itemContext = context.get(LycheeContextKey.ITEM);
		for (var index : indexes) {
			var holder = itemContext.get(index);
			var base = getPath().isEmpty()
					? this.itemStack.copy()
					: ItemStack.parseOptional(
							registryAccess,
							CommonProxy.jsonToTag(new JsonPointer(getPath().get()).find(context.get(LycheeContextKey.JSON))));
			if (base.isEmpty()) {
				context.setItem(index, ItemStack.EMPTY);
				holder.setConsumption(0);
				continue;
			}
			var stack = holder.get();
			var consumption = holder.getConsumption();
			if (stack.isEmpty() || consumption <= 0) {
				continue;
			}
			holder.split(Math.min(times * consumption, stack.getCount()));
			holder.setConsumption(0);
			placeOutput(itemContext, holder, base, times);
		}
	}

	private void placeOutput(ItemStackHolderCollection itemContext, ExtendedItemStackHolder holder, ItemStack base, int times) {
		var total = base.getCount() * times;
		while (total > 0) {
			var output = base.copy();
			output.setCount(Math.min(total, base.getMaxStackSize()));
			total -= output.getCount();
			if (!placeAtSlot(holder, output)) {
				itemContext.stacksNeedHandle.add(output);
			}
		}
	}

	private boolean placeAtSlot(ExtendedItemStackHolder holder, ItemStack output) {
		var current = holder.get();
		if (current.isEmpty()) {
			holder.set(output);
			return true;
		}
		if (!ItemStack.isSameItemSameComponents(current, output)) {
			return false;
		}
		var space = current.getMaxStackSize() - current.getCount();
		if (space <= 0) {
			return false;
		}
		var moved = Math.min(output.getCount(), space);
		current.grow(moved);
		output.shrink(moved);
		return output.isEmpty();
	}

	@Override
	public Component getDisplayName() {
		return itemStack.getHoverName();
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return List.of(itemStack);
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	@Override
	public ItemStack transformRemainder(ItemStack itemStack, @Nullable ILycheeRecipe<?> recipe) {
		return itemStack();
	}

	//	@Override
	//	public JsonElement provideJsonInfo(ILycheeRecipe<?> recipe, JsonPointer pointer, JsonObject recipeObject) {
	//		setPath(pointer.toString());
	//		return CommonProxy.tagToJson(stack.save(new CompoundTag()));
	//	}

	public static class Type implements PostActionType<SetItem> {
		public static final MapCodec<SetItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(SetItem::commonProperties),
				LycheeCodecs.ITEM_STACK_MAP_CODEC.forGetter(SetItem::itemStack),
				Reference.CODEC.optionalFieldOf("target", Reference.DEFAULT).forGetter(SetItem::target)
		).apply(instance, SetItem::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, SetItem> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				SetItem::commonProperties,
				ItemStack.OPTIONAL_STREAM_CODEC,
				SetItem::itemStack,
				Reference.STREAM_CODEC,
				SetItem::target,
				SetItem::new);

		@Override
		public MapCodec<SetItem> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SetItem> streamCodec() {
			return STREAM_CODEC;
		}
	}
}