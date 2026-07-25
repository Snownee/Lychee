package snownee.lychee.action;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LootContextKeys;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Displays;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record InsertItem(
		PostActionCommonProperties commonProperties,
		Optional<ItemStackTemplate> item,
		Optional<Reference> from,
		boolean dropIfFail) implements PostAction {

	public InsertItem {
		Preconditions.checkArgument(item.isPresent() != from.isPresent(), "Exactly one of 'item' and 'from' must be specified");
	}

	@Override
	public PostActionType<InsertItem> type() {
		return PostActionTypes.INSERT_ITEM;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		var pos = actionContext.get(LootContextKeys.BLOCK_POS);
		var level = context.level();
		var direction = actionContext.getOrNull(LootContextKeys.DIRECTION);

		var toInsert = prepareItemStack(context, times);
		if (toInsert == null) {
			return;
		}

		var totalCount = toInsert.getCount();
		var inserted = CommonProxy.insertItem(level, pos, direction, toInsert);
		if (inserted < totalCount && dropIfFail && !toInsert.isEmpty()) {
			var origin = actionContext.getOrNull(LootContextParams.ORIGIN);
			var dropX = origin != null ? origin.x : pos.getX() + 0.5;
			var dropY = origin != null ? origin.y : pos.getY() + 0.5;
			var dropZ = origin != null ? origin.z : pos.getZ() + 0.5;
			CommonProxy.dropItemStack(level, dropX, dropY, dropZ, toInsert, null);
		}
	}

	@Nullable
	private ItemStack prepareItemStack(LycheeContext context, int times) {
		if (item.isPresent()) {
			var stack = item.get().create();
			stack.setCount(stack.getCount() * times);
			return stack;
		}
		var recipe = context.get(LycheeContextKey.RECIPE);
		var indexes = recipe.getItemIndexes(from.orElseThrow());
		if (indexes.isEmpty()) {
			return null;
		}
		var itemContext = context.get(LycheeContextKey.ITEM);
		var holder = itemContext.get(indexes.getInt(0));
		var maxCount = Math.min(holder.get().getCount(), times * holder.get().getMaxStackSize());
		holder.setConsumption(0);
		return itemContext.split(indexes.getInt(0), maxCount).get();
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public boolean hidden() {
		return item.isEmpty();
	}

	@Override
	public Component getName() {
		return item.map(ItemStackTemplate::create).map(ItemStack::getHoverName).orElseGet(PostAction.super::getName);
	}

	@Override
	public List<SlotDisplay> getOutputItems() {
		return item.map(i -> List.of(Displays.slot(i))).orElse(List.of());
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		from.ifPresent(reference -> Preconditions.checkArgument(
				!recipe.getItemIndexes(reference).isEmpty(),
				"No target found for %s",
				reference));
	}

	public static class Type implements PostActionType<InsertItem> {
		public static final MapCodec<InsertItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(InsertItem::commonProperties),
				LycheeCodecs.ITEM_STACK_TEMPLATE.optionalFieldOf("item").forGetter(InsertItem::item),
				Reference.CODEC.optionalFieldOf("from").forGetter(InsertItem::from),
				Codec.BOOL.optionalFieldOf("drop_if_fail", true).forGetter(InsertItem::dropIfFail)).apply(instance, InsertItem::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, InsertItem> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				InsertItem::commonProperties,
				ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional),
				InsertItem::item,
				Reference.STREAM_CODEC.apply(ByteBufCodecs::optional),
				InsertItem::from,
				ByteBufCodecs.BOOL,
				InsertItem::dropIfFail,
				InsertItem::new);

		@Override
		public MapCodec<InsertItem> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, InsertItem> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
